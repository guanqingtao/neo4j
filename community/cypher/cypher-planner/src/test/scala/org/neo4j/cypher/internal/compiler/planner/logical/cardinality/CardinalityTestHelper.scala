/*
 * Copyright (c) 2002-2019 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.cypher.internal.compiler.planner.logical.cardinality

import org.neo4j.cypher.internal.compiler.helpers.MapSupport._
import org.neo4j.cypher.internal.compiler.planner.LogicalPlanningTestSupport
import org.neo4j.cypher.internal.compiler.planner.logical.QueryGraphProducer
import org.neo4j.cypher.internal.ir.{QueryGraph, StrictnessMode}
import org.neo4j.cypher.internal.planner.spi.{GraphStatistics, IndexDescriptor, MinimumGraphStatistics}
import org.neo4j.cypher.internal.v4_0.ast.semantics.SemanticTable
import org.neo4j.cypher.internal.v4_0.expressions.Variable
import org.neo4j.cypher.internal.v4_0.util.Cardinality.NumericCardinality
import org.neo4j.cypher.internal.v4_0.util._
import org.neo4j.cypher.internal.v4_0.util.test_helpers.CypherFunSuite
import org.scalatest.matchers.MatchResult
import org.scalatest.matchers.Matcher

import scala.collection.mutable

trait CardinalityTestHelper extends QueryGraphProducer with CardinalityCustomMatchers {
  // This does not support composite indexes, but the only test that uses this will not differentiate
  // between single property and composite indexes anyway, so adding support here will not improve test coverage.
  // Adding support for composite indexes to AssumeIndependenceQueryGraphCardinalityModelTest will not improve coverage either
  // because it only verifies that you get back what you put in, which means we will just be testing the test framework.

  self: CypherFunSuite with LogicalPlanningTestSupport =>

  import SemanticTableHelper._

  def combiner: SelectivityCombiner = IndependenceCombiner

  def not(number: Double) = Selectivity.of(number).getOrElse(Selectivity.ONE).negate.factor
  def and(numbers: Double*) = combiner.andTogetherSelectivities(numbers.map(Selectivity.of(_).getOrElse(Selectivity.ONE))).get.factor
  def or(numbers: Double*) = combiner.orTogetherSelectivities(numbers.map(Selectivity.of(_).getOrElse(Selectivity.ONE))).get.factor
  def orTimes(times: Int, number: Double) = combiner.orTogetherSelectivities(1.to(times).map(_ => Selectivity.of(number).get)).get.factor

  def degree(above: Double, below: Double) = above / below

  case class TestUnit(query: String,
                      allNodes: Option[Double] = None,
                      knownLabelCardinality: Map[String, Double] = Map.empty,
                      knownIndexSelectivity: Map[(String, String), Double] = Map.empty,
                      knownIndexPropertyExistsSelectivity: Map[(String, String), Double] = Map.empty,
                      knownProperties: Set[String] = Set.empty,
                      knownRelationshipCardinality: Map[(String, String, String), Double] = Map.empty,
                      knownNodeNames: Set[String] = Set.empty,
                      knownRelNames: Set[String] = Set.empty,
                      queryGraphArgumentIds: Set[String] = Set.empty,
                      inboundCardinality: Cardinality = Cardinality(1),
                      strictness: Option[StrictnessMode] = None) {

    self =>

    def withNodeName(nodeName: String) = copy(knownNodeNames = knownNodeNames + nodeName)

    def withRelationshipName(relName: String) = copy(knownRelNames = knownRelNames + relName)

    def withInboundCardinality(d: Double) = copy(inboundCardinality = Cardinality(d))

    def withLabel(tuple: (Symbol, Double)): TestUnit = copy(knownLabelCardinality = knownLabelCardinality + (tuple._1.name -> tuple._2))

    def withLabel(label: Symbol, cardinality: Double): TestUnit = copy(knownLabelCardinality = knownLabelCardinality + (label.name -> cardinality))

    def addWithLabels(cardinality: Double, labels: Symbol*) = {
      val increments = labels.map { label => label.name -> cardinality }.toMap
      copy(knownLabelCardinality = knownLabelCardinality.fuse(increments)(_ + _))
    }

    def withQueryGraphArgumentIds(idNames: String*): TestUnit =
      copy(queryGraphArgumentIds = Set(idNames: _*))

    def withGraphNodes(number: Double): TestUnit = copy(allNodes = Some(number))


    def withRelationshipCardinality(relationship: (((Symbol, Symbol), Symbol), Double)): TestUnit = {
      val (((lhs, relType), rhs), cardinality) = relationship
      val key = (lhs.name, relType.name, rhs.name)
      assert(!knownRelationshipCardinality.contains(key), "This label/type/label combo is already known")
      copy (
        knownRelationshipCardinality = knownRelationshipCardinality + (key -> cardinality)
      )
    }

    def addRelationshipCardinality(relationship: (((Symbol, Symbol), Symbol), Double)): TestUnit = {
      val (((lhs, relType), rhs), cardinality) = relationship
      val key = (lhs.name, relType.name, rhs.name)
      val increment = Map(key -> cardinality)
      copy (
        knownRelationshipCardinality = knownRelationshipCardinality.fuse(increment)(_ + _)
      )
    }

    def withIndexSelectivity(v: ((Symbol, Symbol), Double)) = {
      val ((Symbol(labelName), Symbol(propertyName)), selectivity) = v
      if (!knownLabelCardinality.contains(labelName))
        fail("Label not known. Add it with withLabel")

      copy(
        knownIndexSelectivity = knownIndexSelectivity + ((labelName, propertyName) -> selectivity),
        knownProperties = knownProperties + propertyName
      )
    }

    def withIndexPropertyExistsSelectivity(v: ((Symbol, Symbol), Double)) = {
      val ((Symbol(labelName), Symbol(propertyName)), selectivity) = v
      if (!knownLabelCardinality.contains(labelName))
        fail("Label not known. Add it with withLabel")

      copy(
        knownIndexPropertyExistsSelectivity = knownIndexPropertyExistsSelectivity + ((labelName, propertyName) -> selectivity),
        knownProperties = knownProperties + propertyName
      )
    }

    def withKnownProperty(propertyName: Symbol) =
      copy(
        knownProperties = knownProperties + propertyName.name
      )

    def prepareTestContext:(GraphStatistics, SemanticTable) = {
      val labelIds: Map[String, Int] = knownLabelCardinality.keys.zipWithIndex.toMap
      val propertyIds: Map[String, Int] = knownProperties.zipWithIndex.toMap
      val relTypeIds: Map[String, Int] = knownRelationshipCardinality.map(_._1._2).toSeq.distinct.zipWithIndex.toMap

      val statistics = new GraphStatistics {

        override def nodesAllCardinality(): Cardinality = allNodes.getOrElse(fail("All nodes not set"))

        def nodesWithLabelCardinality(labelId: Option[LabelId]): Cardinality =
          Cardinality({
            labelId
              .flatMap(getLabelName)
              .map(knownLabelCardinality)
              .getOrElse(MinimumGraphStatistics.MIN_NODES_WITH_LABEL)
          })

        def uniqueValueSelectivity(index: IndexDescriptor): Option[Selectivity] = {
          val labelName: Option[String] = getLabelName(index.label)
          val propertyName: Option[String] = getPropertyName(index.property)
          (labelName, propertyName) match {
            case (Some(lName), Some(pName)) =>
              val selectivity = knownIndexSelectivity.get((lName, pName))
              selectivity.map(Selectivity.of(_).getOrElse(Selectivity.ONE))

            case _ => Some(Selectivity.ZERO)
          }
        }

        def indexPropertyExistsSelectivity(index: IndexDescriptor): Option[Selectivity] = {
          val labelName: Option[String] = getLabelName(index.label)
          val propertyName: Option[String] = getPropertyName(index.property)
          (labelName, propertyName) match {
            case (Some(lName), Some(pName)) =>
              val selectivity = knownIndexPropertyExistsSelectivity.get((lName, pName))
              selectivity.map(s => Selectivity.of(s).get)

            case _ => Some(Selectivity.ZERO)
          }
        }

        def getCardinality(fromLabel:String, typ:String, toLabel:String): Double =
          knownRelationshipCardinality.getOrElse((fromLabel, typ, toLabel), 0.0)

        def patternStepCardinality(fromLabel: Option[LabelId], relTypeId: Option[RelTypeId], toLabel: Option[LabelId]): Cardinality =
          (fromLabel, relTypeId, toLabel) match {
            case (_, Some(id), _) if getRelationshipName(id).isEmpty => Cardinality(0)
            case (Some(id), _, _) if getLabelName(id).isEmpty        => Cardinality(0)
            case (_, _, Some(id)) if getLabelName(id).isEmpty        => Cardinality(0)

            case (l1, t1, r1) =>
              val matchingCardinalities = knownRelationshipCardinality collect {
                case ((l2, t2, r2), c) if
                l1.forall(x => getLabelName(x).get == l2) &&
                  t1.forall(x => getRelationshipName(x).get == t2) &&
                  r1.forall(x => getLabelName(x).get == r2) => c
              }

              if (matchingCardinalities.isEmpty)
                Cardinality(0)
              else
                Cardinality(matchingCardinalities.sum)
          }

        private def getLabelName(labelId: LabelId) = labelIds.collectFirst {
          case (name, id) if id == labelId.id => name
        }

        private def getRelationshipName(relTypeId: RelTypeId) = relTypeIds.collectFirst {
          case (name, id) if id == relTypeId.id => name
        }

        private def getPropertyName(propertyId: PropertyKeyId) = propertyIds.collectFirst {
          case (name, id) if id == propertyId.id => name
        }
      }

      val semanticTable: SemanticTable = {
        val empty = SemanticTable()
        val withNodes = knownNodeNames.foldLeft(empty) { case (table, node) => table.addNode(Variable(node)(pos)) }
        val withNodesAndRels = knownRelNames.foldLeft(withNodes) { case (table, rel) => table.addRelationship(Variable(rel)(pos)) }
        withNodesAndRels
      }

      fill(semanticTable.resolvedLabelNames, labelIds, LabelId.apply)
      fill(semanticTable.resolvedPropertyKeyNames, propertyIds, PropertyKeyId.apply)
      fill(semanticTable.resolvedRelTypeNames, relTypeIds, RelTypeId.apply)

      (statistics, semanticTable)
    }

    private def fill[T](destination: mutable.Map[String, T], source: Iterable[(String, Int)], f: Int => T) {
      source.foreach {
        case (name, id) => destination += name -> f(id)
      }
    }

    def createQueryGraph(semanticTable: SemanticTable): (QueryGraph, SemanticTable) = {
      val (plannerQuery, rewrittenTable) = producePlannerQueryForPattern(query)
      (plannerQuery.lastQueryGraph.withArgumentIds(queryGraphArgumentIds), semanticTable.transplantResolutionOnto(rewrittenTable))
    }
  }
}

trait CardinalityCustomMatchers {

  class MapEqualityWithDouble[T](expected: Map[T, Cardinality], tolerance: Double)(implicit num: Numeric[Cardinality])
    extends Matcher[Map[T, Cardinality]] {

    def apply(other: Map[T, Cardinality]) = {
      MatchResult(
        expected.size == other.size && expected.foldLeft(true) {
          case (acc, (key, value)) =>
            import Cardinality._
            import org.scalactic.Tolerance._
            import org.scalactic.TripleEquals._
            acc && other.contains(key) && other(key) === value +- tolerance
        },
        s"""$other did not equal "$expected" wrt a tolerance of $tolerance""",
        s"""$other equals "$expected" wrt a tolerance of $tolerance"""
      )
    }

  }

  def equalWithTolerance[T](expected: Map[T, Cardinality], tolerance: Double): Matcher[Map[T, Cardinality]] = {
    new MapEqualityWithDouble[T](expected, tolerance)(NumericCardinality)
  }
}

object SemanticTableHelper {
  implicit class RichSemanticTable(table: SemanticTable) {
    def transplantResolutionOnto(target: SemanticTable): SemanticTable =
      target.copy(
        resolvedLabelIds = table.resolvedLabelNames,
        resolvedPropertyKeyNames = table.resolvedPropertyKeyNames,
        resolvedRelTypeNames = table.resolvedRelTypeNames
      )
  }
}
