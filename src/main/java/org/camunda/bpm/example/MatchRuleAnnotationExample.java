/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.example;

import java.io.IOException;
import java.io.InputStream;

import org.camunda.bpm.dmn.engine.DmnDecision;
import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.delegate.DmnDecisionTableEvaluationEvent;
import org.camunda.bpm.dmn.engine.delegate.DmnEvaluatedDecisionRule;
import org.camunda.bpm.dmn.engine.delegate.DmnEvaluatedOutput;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.dmn.Dmn;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.instance.DecisionRule;

public class MatchRuleAnnotationExample {

  public static void main(String[] args) {
    // create evaluation listener to record matched rules
    DecisionTableEvaluationListener evaluationListener = new DecisionTableEvaluationListener();

    // create a new default DMN engine
    DmnEngineConfiguration engineConfiguration = DmnEngineConfiguration.createDefaultDmnEngineConfiguration();
    engineConfiguration.getCustomPostDecisionTableEvaluationListeners().add(evaluationListener);
    DmnEngine dmnEngine = engineConfiguration.buildEngine();

    // parse decision from resource input stream
    InputStream inputStream = MatchRuleAnnotationExample.class.getResourceAsStream("/example.dmn");
    DmnModelInstance dmnModelInstance = Dmn.readModelFromStream(inputStream);

    try {
      DmnDecision decision = dmnEngine.parseDecision("decision", dmnModelInstance);
      System.out.println("For input = 1");
      evaluteDecision(Variables.putValue("input", 1), evaluationListener, dmnEngine, dmnModelInstance, decision);
      System.out.println("For input = 2");
      evaluteDecision(Variables.putValue("input", 2), evaluationListener, dmnEngine, dmnModelInstance, decision);
      System.out.println("For input = 3");
      evaluteDecision(Variables.putValue("input", 3), evaluationListener, dmnEngine, dmnModelInstance, decision);
    }
    finally {
      try {
        inputStream.close();
      }
      catch (IOException e) {
        System.err.println("Could not close stream: "+e.getMessage());
      }
    }
  }

  public static void evaluteDecision(VariableMap variables, DecisionTableEvaluationListener evaluationListener, DmnEngine dmnEngine, DmnModelInstance dmnModelInstance, DmnDecision decision) {
    // evaluate decision
    dmnEngine.evaluateDecisionTable(decision, variables);

    // print matched rule annotations
    for (DmnEvaluatedDecisionRule matchingRule : evaluationListener.getLastEvent().getMatchingRules()) {
      DecisionRule rule = dmnModelInstance.getModelElementById(matchingRule.getId());
      System.out.println(rule.getDescription().getTextContent() + " matched");
    }
  }


}
