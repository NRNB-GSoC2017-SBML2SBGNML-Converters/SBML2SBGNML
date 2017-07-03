/*******************************************************************************
 * Copyright 2016 Kaito Ii
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
/*
 * 
 */
package org.sbfc.converter.sbgnml2sbml;

import javax.xml.stream.XMLStreamException;

import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLWriter;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Unit;
import org.sbml.jsbml.Unit.Kind;
import org.sbml.jsbml.UnitDefinition;

// TODO: Auto-generated Javadoc
/**
 * The Class SBMLModelCompleter.
 *
 * @author Kaito Ii
 *         Date Created: Jun 9, 2016
 */
public class SBMLModelCompleter {

  /** The document. */
  static SBMLDocument document;
  /** The model. */
  static Model        model;
  /** The Constant level. */
  static final int    level   = 3;
  /** The Constant version. */
  static final int    version = 1;


  /**
   * Auto complete required attributes.
   *
   * @param doc
   *        the doc
   * @return SBMLDocument
   *         TODO
   */
  public static SBMLDocument autoCompleteRequiredAttributes(SBMLDocument doc) {
    document = doc;

    // Namespace correction in the document: remove references to the previous
    // Level and Version combination:
//    document.getDeclaredNamespaces().remove("xmlns");
//    document.addDeclaredNamespace("xmlns",
//      String.format("http://www.sbml.org/sbml/level%d/version%d/core",
//        level, version));

    // Now proceed with the model:
    model = document.getModel();
    if (model.isSetListOfUnitDefinitions()) {
      completeUnitDefinitions(model.getListOfUnitDefinitions());
    }
    if (model.isSetListOfCompartments()) {
      completeCompartments(model.getListOfCompartments());
    }
    if (model.isSetListOfReactions()) {
      completeReactions(model.getListOfReactions());
    }
    if (model.isSetListOfSpecies()) {
      completeSpecies(model.getListOfSpecies());
    }
    if (model.isSetListOfParameters()) {
      completeParameter(model.getListOfParameters());
    }

    // This saves us the effort of explicitly defining the units of individual
    // model components:
    if (model.findUnitDefinition(UnitDefinition.SUBSTANCE) != null) {
      model.setSubstanceUnits(UnitDefinition.SUBSTANCE);
      model.setExtentUnits(model.getSubstanceUnits());
    }
    if (model.findUnitDefinition(UnitDefinition.AREA) != null) {
      model.setAreaUnits(UnitDefinition.AREA);
    }
    if (model.findUnitDefinition(UnitDefinition.VOLUME) != null) {
      model.setVolumeUnits(UnitDefinition.VOLUME);
    }
    if (model.findUnitDefinition(UnitDefinition.LENGTH) != null) {
      model.setLengthUnits(UnitDefinition.LENGTH);
    }
    if (model.findUnitDefinition(UnitDefinition.TIME) != null) {
      model.setTimeUnits(UnitDefinition.TIME);
    }

    return document;
  }


  /**
   * Complete unit definitions.
   *
   * @param unitDefinitions
   *        the unit definitions
   * @return ListOf<UnitDefinition>
   *         TODO
   */
  public static ListOf<UnitDefinition> completeUnitDefinitions(
    ListOf<UnitDefinition> unitDefinitions) {
    for (UnitDefinition ud : unitDefinitions) {
      ListOf<Unit> units = ud.getListOfUnits();
      for (Unit u : units) {
        if (!u.isSetExponent()) {
          u.setExponent(1d);
        }
        if (!u.isSetMultiplier()) {
          u.setMultiplier(1d);
        }
        if (!u.isSetKind()) {
          u.setKind(Kind.INVALID);
        }
        if (!u.isSetScale()) {
          u.setScale(0);
        }
      }
    }
    return unitDefinitions;
  }


  /**
   * Complete compartments.
   *
   * @param compartments
   *        the compartments
   * @return ListOf<Compartment>
   *         TODO
   */
  public static ListOf<Compartment> completeCompartments(
    ListOf<Compartment> compartments) {
    int i = 0;
    for (Compartment c : compartments) {
      if (!c.isSetId()) {
        c.setId(c.getClass().getSimpleName() + i++);
      }
      if (!c.isSetConstant()) {
        c.setConstant(true);
      }
    }
    return compartments;
  }


  /**
   * Complete species.
   *
   * @param species
   *        the species
   * @return ListOf<Species>
   *         TODO
   */
  public static ListOf<Species> completeSpecies(ListOf<Species> species) {
    int i = 0;
    for (Species s : species) {
      if (!s.isSetId()) {
        s.setId(s.getClass().getSimpleName() + i++);
      }
      if (!s.isSetCompartment()) {
        s.setCompartment(model.getCompartment(0));
      }
      if (!s.isSetHasOnlySubstanceUnits()) {
        s.setHasOnlySubstanceUnits(true);
      }
      if (!s.isSetBoundaryCondition()) {
        s.setBoundaryCondition(true);
      }
      if (!s.isSetConstant()) {
        s.setConstant(true);
      }
    }
    return species;
  }


  /**
   * Complete parameter.
   *
   * @param parameters
   *        the parameters
   * @return ListOf<Parameter>
   *         TODO
   */
  public static ListOf<Parameter> completeParameter(ListOf<Parameter> parameters) {
    int i = 0;
    for (Parameter p : parameters) {
      if (!p.isSetId()) {
        p.setId(p.getClass().getSimpleName() + i++);
      }
      if (!p.isSetConstant()) {
        p.setConstant(true);
      }
    }
    return parameters;
  }


  /**
   * Complete reactions.
   *
   * @param reactions
   *        the reactions
   * @return ListOf<Reaction>
   *         TODO
   */
  public static ListOf<Reaction> completeReactions(ListOf<Reaction> reactions) {
    int i = 0;
    for (Reaction r : reactions) {
      if (!r.isSetId()) {
        r.setId(r.getClass().getSimpleName() + i++);
      }
      if (!r.isSetFast()) {
        r.setFast(true);
      }
      if (!r.isSetReversible()) {
        r.setReversible(true);
      }
      completeSpeciesReference(r.getListOfReactants());
      completeSpeciesReference(r.getListOfProducts());
      completeModifierSpeciesReference(r.getListOfModifiers());
    }
    return reactions;
  }


  /**
   * Complete species reference.
   *
   * @param speciesReference
   *        the species reference
   * @return ListOf<SpeciesReference>
   *         TODO
   */
  public static ListOf<SpeciesReference> completeSpeciesReference(
    ListOf<SpeciesReference> speciesReference) {
    for (SpeciesReference sr : speciesReference) {
      if (!sr.isSetSpecies()){
        Species s = model.getSpecies(sr.getId());
        if(s == null) {
          sr.setSpecies(model.getSpecies(0));
        } else {
          sr.setSpecies(s);
        }
      }
      if (!sr.isSetStoichiometry()) {
        sr.setStoichiometry(1d);
      }
      if (!sr.isSetConstant()) {
        sr.setConstant(true);
      }
    }
    return speciesReference;
  }


  /**
   * Complete modifier species reference.
   *
   * @param modifierSpeciesReference
   *        the modifier species reference
   * @return ListOf<ModifierSpeciesReference>
   *         TODO
   */
  public static ListOf<ModifierSpeciesReference> completeModifierSpeciesReference(
    ListOf<ModifierSpeciesReference> modifierSpeciesReference) {
    for (ModifierSpeciesReference sr : modifierSpeciesReference) {
      if (!sr.isSetSpecies()){
        Species s = model.getSpecies(sr.getId());
        if(s == null) {
          sr.setSpecies(model.getSpecies(0));
        } else {
          sr.setSpecies(s);
        }
      }
    }
    return modifierSpeciesReference;
  }
}
