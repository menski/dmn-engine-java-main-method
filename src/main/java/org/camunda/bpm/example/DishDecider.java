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
import java.util.List;
import java.util.Map;

import org.camunda.bpm.dmn.engine.*;
import org.camunda.bpm.dmn.engine.delegate.DmnDecisionTableEvaluationEvent;
import org.camunda.bpm.dmn.engine.delegate.DmnEvaluatedDecisionRule;
import org.camunda.bpm.dmn.engine.delegate.DmnEvaluatedOutput;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;

public class DishDecider {

  public static void printUsage(String errorMessage, int exitCode) {
    System.err.println("Error: " + errorMessage);
    System.err.println("Usage: java -jar DishDecider.jar SEASON GUEST_COUNT\n\n\tSEASON: the current season (Spring, Summer, Fall or Winter)\n\tGUEST_COUNT: number of guest to expect");
    System.exit(exitCode);
  }

  public static void main(String[] args) {
    // parse arguments
    if (args.length != 2) {
      printUsage("Please specify the current season and the expected number of guests.", 1);
    }
    String season = args[0];

    Integer guestCount = 0;
    try  {
      guestCount = Integer.parseInt(args[1]);
    }
    catch (NumberFormatException e) {
      printUsage("The number of expected guests must be a number", 2);
    }

    // prepare variables for decision evaluation
    VariableMap variables = Variables
      .putValue("season", season)
      .putValue("guestCount", guestCount);

    // create evaluation listner to record matched rules
    DishDecisionTableEvaluationListener evaluationListener = new DishDecisionTableEvaluationListener();

    // create a new default DMN engine
    DmnEngineConfiguration engineConfiguration = DmnEngineConfiguration.createDefaultDmnEngineConfiguration();
    engineConfiguration.getCustomPostDecisionTableEvaluationListeners().add(evaluationListener);
    DmnEngine dmnEngine = engineConfiguration.buildEngine();

    // parse decision from resource input stream
    InputStream inputStream = DishDecider.class.getResourceAsStream("dish-decision.dmn11.xml");

    try {
      DmnDecision decision = dmnEngine.parseDecision("decision", inputStream);

      // evaluate decision
      DmnDecisionTableResult result = dmnEngine.evaluateDecisionTable(decision, variables);

      // print result
      String desiredDish = result.getSingleResult().getSingleEntry();
      System.out.println("Dish Decision:\n\tI would recommend to serve: " + desiredDish);

      // print event
      DmnDecisionTableEvaluationEvent evaluationEvent = evaluationListener.getLastEvent();
      System.out.println("The following Rules matched:");
      for (DmnEvaluatedDecisionRule matchedRule : evaluationEvent.getMatchingRules()) {
        System.out.println("\t" + matchedRule.getId() + ":");
        for (DmnEvaluatedOutput output : matchedRule.getOutputEntries().values()) {
          System.out.println("\t\t" + output);
        }
      }

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


}
