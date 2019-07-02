package averroes.frameworks.soot

import soot.jimple._
import soot._
import soot.javaToJimple.LocalGenerator

import scala.collection.mutable
import scala.jdk.CollectionConverters._

object LocalOptimizer {

  abstract class FlowNode {
    def isLocal = false
  }
  case class ValueNode(value: Value) extends FlowNode {
    override def isLocal = value.isInstanceOf[Local]
    override def hashCode = value.equivHashCode
    override def equals(o: Any) = o match {
      case ValueNode(ov) => value equivTo ov
      case _ => false
    }
  }
  case object ReturnNode extends FlowNode
  case object ThrowNode extends FlowNode

  case class ArgumentNode(target: SootMethod, index: Int) extends FlowNode
  case class ReceiverNode(target: SootMethod) extends FlowNode
  case class CallReturnNode(target: SootMethod) extends FlowNode

  case class FieldWriteReceiverNode(field: SootField) extends FlowNode
  case class FieldWriteValueNode(field: SootField) extends FlowNode
  case class FieldReadReceiverNode(field: SootField) extends FlowNode
  case class FieldReadValueNode(field: SootField) extends FlowNode

  case class ArrayWriteReceiverNode(elementType: Type) extends FlowNode
  case class ArrayWriteValueNode(elementType: Type) extends FlowNode
  case class ArrayReadReceiverNode(elementType: Type) extends FlowNode
  case class ArrayReadValueNode(elementType: Type) extends FlowNode

  case class MethodSummary(flowEdges: Set[(FlowNode, FlowNode)], calledMethods: Set[SootMethod])

  def summarize(method: SootMethod): MethodSummary = {

    val body = method.getActiveBody
    val units = body.getUnits

    val edges = mutable.Set[(FlowNode, FlowNode)]()
    val calledMethods = mutable.Set[SootMethod]()

    def sourceNode(v: Value): FlowNode = v match {
      case ce: CastExpr => sourceNode(ce.getOp)
      case fr: InstanceFieldRef =>
        edges.add(FieldReadReceiverNode(fr.getField), sourceNode(fr.getBase))
        FieldReadValueNode(fr.getField)
      case ar: ArrayRef =>
        val elementType = ar.getType
        edges.add(ArrayReadReceiverNode(elementType), sourceNode(ar.getBase))
        ArrayReadValueNode(elementType)
      case _ => ValueNode(v)
    }
    def targetNode(v: Value): FlowNode = v match {
      case fr: InstanceFieldRef =>
        edges.add(FieldWriteReceiverNode(fr.getField), sourceNode(fr.getBase))
        FieldWriteValueNode(fr.getField)
      case ar: ArrayRef =>
        val elementType = ar.getType
        edges.add(ArrayWriteReceiverNode(elementType), sourceNode(ar.getBase))
        ArrayWriteValueNode(elementType)
      case _ => ValueNode(v)
    }

    for (u <- units.asScala) {
      u match {
        case ds: DefinitionStmt if !ds.containsInvokeExpr =>
          edges.add(targetNode(ds.getLeftOp), sourceNode(ds.getRightOp))
        case r: ReturnStmt =>
          edges.add(ReturnNode, sourceNode(r.getOp))
        case ts: ThrowStmt =>
          edges.add(ThrowNode, sourceNode(ts.getOp))
        case _ =>
      }
      u match {
        case s: Stmt if s.containsInvokeExpr =>
          val ie = s.getInvokeExpr
          val target = ie.getMethod
          calledMethods.add(target)
          for(i <- 0 until ie.getArgCount) {
            edges.add(ArgumentNode(target, i), sourceNode(ie.getArg(i)))
          }
          ie match {
            case iie: InstanceInvokeExpr =>
              edges.add(ReceiverNode(target), sourceNode(iie.getBase))
            case _ =>
          }
          s match {
            case ds: DefinitionStmt =>
              edges.add(targetNode(ds.getLeftOp), CallReturnNode(target))
            case _ =>
          }
        case _ =>
      }
    }

    MethodSummary(edges.toSet, calledMethods.toSet)
  }

  def optimize(summary: MethodSummary): MethodSummary = {
    val edges = mutable.Set[(FlowNode, FlowNode)]() ++ summary.flowEdges
    val targets = summary.flowEdges.map(_._1)
    val sources = summary.flowEdges.map(_._2)
    def flowNodes[T <: FlowNode](nodes: Set[T]): Set[FlowNode] = Set() ++ nodes
    val locals = (flowNodes(targets) intersect flowNodes(sources)).filter(_.isLocal)
    for(k <- locals; i <- sources; j <- targets) {
      if(edges(k,i) && edges(j,k)) edges.add(j,i)
    }
    val noLocals = edges.filterNot{case (t,s) => t.isLocal || s.isLocal}
    MethodSummary(noLocals.toSet, summary.calledMethods)
  }

  def synthesize(method: SootMethod, summary: MethodSummary): scala.Unit = {
    val body = method.getActiveBody
    val locals = body.getLocals
    val units = body.getUnits

    locals.clear()
    units.clear()

    def tpe(node: FlowNode): Type = node match {
      case ValueNode(v) => v.getType
      case ReturnNode => method.getReturnType
      case ThrowNode => RefType.v("java.lang.Throwable")
      case ArgumentNode(target, index) => target.getParameterType(index)
      case ReceiverNode(target) => RefType.v(target.getDeclaringClass)
      case CallReturnNode(target) => target.getReturnType

      case FieldWriteReceiverNode(field) => field.getDeclaringClass.getType
      case FieldWriteValueNode(field) => field.getType
      case FieldReadReceiverNode(field) => field.getDeclaringClass.getType
      case FieldReadValueNode(field) => field.getType

      case ArrayWriteReceiverNode(elementType) => elementType.makeArrayType
      case ArrayWriteValueNode(elementType) => elementType
      case ArrayReadReceiverNode(elementType) => elementType.makeArrayType
      case ArrayReadValueNode(elementType) => elementType
    }

    val edges = summary.flowEdges
    val targets = edges.map(_._1)
    val sources = edges.map(_._2)
    val generator = new LocalGenerator(body)
    def makeLocals[T <: FlowNode](nodes: Set[T]): Map[T, Local] = Map() ++ nodes.map{case node =>
      (node, generator.generateLocal(tpe(node)))
    }
    val sourceLocals = makeLocals(sources)
    val targetLocals = makeLocals(targets)


    def initialConstant(tpe: Type): Value = tpe match {
      case _: RefLikeType => NullConstant.v()
      case _: LongType => LongConstant.v(0)
      case _: IntegerType => IntConstant.v(0)
      case _: FloatType => FloatConstant.v(0)
      case _: DoubleType => DoubleConstant.v(0)
    }

    def wrapInIf(stmt: Stmt): scala.Unit = {
      val nop = Jimple.v().newNopStmt()

      units.add(Jimple.v().newIfStmt(Jimple.v().newEqExpr(IntConstant.v(0), IntConstant.v(0)), nop))
      units.add(stmt)
      units.add(nop)
    }

    def castIfNeeded(d: Value, s: Value) = {
      val rhs =
        if(d.getType == s.getType) s
        else Jimple.v.newCastExpr(s, d.getType)
      Jimple.v().newAssignStmt(d, rhs)
    }

    var thisLocal: Local = null
    if(!method.isStatic) {
      val tpe = method.getDeclaringClass.getType
      thisLocal = generator.generateLocal(tpe)
      units.add(Jimple.v.newIdentityStmt(thisLocal, new ThisRef(tpe)))
    }

    val paramLocals = Map() ++ (0 until method.getParameterCount).map{i =>
      val tpe = method.getParameterType(i)
      (i, generator.generateLocal(tpe))
    }

    for((i, local) <- paramLocals) {
      units.add(Jimple.v.newIdentityStmt(paramLocals(i), new ParameterRef(local.getType, i)))
    }

    for(s <- sources) {
      val rhs = s match {
        case ValueNode(_: ThisRef) => thisLocal
        case ValueNode(pr: ParameterRef) => paramLocals(pr.getIndex)
        case ValueNode(sv) => sv
        case _ => initialConstant(tpe(s))
      }
      units.add(Jimple.v().newAssignStmt(sourceLocals(s), rhs))
    }

    for((d,s) <- edges) units.add(castIfNeeded(targetLocals(d), sourceLocals(s)))

    for(target <- summary.calledMethods) {
      val args: java.util.List[Local] =
        (0 until target.getParameterCount).map{index =>
        targetLocals(ArgumentNode(target, index))}.asJava

      def receiver = targetLocals(ReceiverNode(target))

      val targetRef = target.makeRef
      val ie: InvokeExpr =
        if(target.isStatic)
          Jimple.v().newStaticInvokeExpr(targetRef, args)
        else if(target.isPrivate || target.isConstructor)
          Jimple.v().newSpecialInvokeExpr(receiver, targetRef, args)
        else if(target.getDeclaringClass.isInterface)
          Jimple.v().newInterfaceInvokeExpr(receiver, targetRef, args)
        else
          Jimple.v().newVirtualInvokeExpr(receiver, targetRef, args)

      sourceLocals.get(CallReturnNode(target)) match {
        case Some(dl) =>
          units.add(Jimple.v().newAssignStmt(dl, ie))
        case None =>
          units.add(Jimple.v().newInvokeStmt(ie))
      }
    }

    for(d <- targets) d match {
      case FieldWriteReceiverNode(field) =>
        units.add(Jimple.v().newAssignStmt(
          Jimple.v().newInstanceFieldRef(targetLocals(d), field.makeRef),
          targetLocals(FieldWriteValueNode(field))
        ))
      case FieldReadReceiverNode(field) =>
        units.add(Jimple.v().newAssignStmt(
          sourceLocals(FieldReadValueNode(field)),
          Jimple.v().newInstanceFieldRef(targetLocals(d), field.makeRef)
        ))
      case ArrayWriteReceiverNode(elementType) =>
        units.add(Jimple.v().newAssignStmt(
          Jimple.v().newArrayRef(targetLocals(d), IntConstant.v(0)),
          targetLocals(ArrayWriteValueNode(elementType))
        ))
      case ArrayReadReceiverNode(elementType) =>
        units.add(Jimple.v().newAssignStmt(
          sourceLocals(ArrayReadValueNode(elementType)),
          Jimple.v().newArrayRef(targetLocals(d), IntConstant.v(0))
        ))
      case ValueNode(dv) =>
        units.add(Jimple.v().newAssignStmt(dv, targetLocals(d)))
      case ThrowNode =>
        wrapInIf(Jimple.v().newThrowStmt(targetLocals(d)))
      case _ =>
    }
    if(targets(ReturnNode))
      units.add(Jimple.v().newReturnStmt(targetLocals(ReturnNode)))
    else
      units.add(Jimple.v().newReturnVoidStmt())
  }

  def apply(): scala.Unit = {
    for (cls <- Scene.v.getApplicationClasses.asScala) {
      apply(cls)
    }
  }

  private def apply(cls: SootClass): scala.Unit = {
    for (method <- cls.getMethods.asScala) {
      if (method.hasActiveBody) apply(method)
    }
  }

  private def apply(method: SootMethod): scala.Unit = {
    val summary = summarize(method)
    val optimizedSummary = optimize(summary)
    synthesize(method, optimizedSummary)
  }
}
