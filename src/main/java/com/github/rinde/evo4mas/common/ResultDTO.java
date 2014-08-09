/**
 * 
 */
package com.github.rinde.evo4mas.common;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.github.rinde.rinsim.pdptw.common.StatisticsDTO;

import rinde.jppf.GPComputationResult;

/**
 * @author Rinde van Lon <rinde.vanlon@cs.kuleuven.be>
 * 
 */
public class ResultDTO implements GPComputationResult, Serializable {

  private static final long serialVersionUID = 2053089876856764188L;
  public final String scenarioKey;
  public final StatisticsDTO stats;
  public final float fitness;
  public final String taskDataId;

  public ResultDTO(String scenario, String taskId, StatisticsDTO stat, float fit) {
    scenarioKey = scenario;
    taskDataId = taskId;
    stats = stat;
    fitness = fit;
  }

  @Override
  public float getFitness() {
    return fitness;
  }

  @Override
  public String getTaskDataId() {
    return taskDataId;
  }

  @Override
  public String toString() {
    return ReflectionToStringBuilder.toString(this);
  }
}
