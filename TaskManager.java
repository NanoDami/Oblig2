import java.util.Arrays;

public class TaskManager {

  Task taskTree;
  int numberOfNodes;

  public TaskManager(String fileName) {

    TaskScanner scanner = new TaskScanner();
    scanner.scanFile(fileName);
    this.taskTree = scanner.getTaskTree();//The task tree is still unoptimized
    this.numberOfNodes = scanner.getTotalTasks();

    //Check for circularity
    if (isCircular(this.taskTree)) {

      System.out.println("\nThis tree is circular! Aborting!");
      System.exit(-1);

    }

    taskTree.leaveAll();

    int totalTime = this.findCriticalPath(this.taskTree);
    optimizeTasksOnTime(taskTree);

    int i = 0;
    while (i <= totalTime) {

      System.out.println("Tick " + i + ":");
      taskTree.tick(i++);

      System.out.println("Number of workers on project: " + Task.nWorkers);

      System.out.println("----------------\n");

    }

    System.out.println("Total time was: " + totalTime + "\n");

  }

  //This function prints the loop and back to the startnode 0
  public boolean isCircular(Task taskTree) {

    boolean result = false;

    if (taskTree.hasBeenVisited()) {
        System.out.print("This tree is circular at the chain:\n\t" + taskTree.getID());
        taskTree.leave();
        return true;
    }

    taskTree.visit();

    for (Task t : taskTree.getOutEdges()) result = result || isCircular(t);

    taskTree.leave();

    if (result) System.out.print(" <- " + taskTree.getID());

    return result;

  }

  public void optimizeTasksOnTime(Task taskTree) {

    if (taskTree == null) return;

    if (taskTree.getID() == 0) {

      for (Task t : taskTree.getOutEdges()) {

        optimizeTasksOnTime(t);

      }

    }

    int maxTime = 0, maxStart = 0;
    for (Task t : taskTree.getDependencyEdges()) {

      int time = t.getEstimatedTime();
      int start = t.getEarliestStart();

      //Nope
      /*if (t.isCritical()) {

        maxTime = t.getEstimatedTime();
        maxStart = t.getEarliestStart();

      }*/

      if (time >= maxTime) {

        maxTime = time;
        maxStart = start;

      }

    }

    taskTree.setEarliestStart(maxTime+maxStart);
    Task[] taskLevel = taskTree.getOutEdges();
    Task criticalTask = null;

    for (Task t : taskLevel) {

      optimizeTasksOnTime(t);
      if (t.isCritical()) criticalTask = t;

    }

    if (criticalTask == null) return;

    for (Task t : taskLevel) {

      t.setLatestStart(t.getEarliestStart() + (criticalTask.getEstimatedTime()-t.getEstimatedTime()));

    }

  }

  public void optimizeTasksOnTimeOLD(Task taskTree) {

    if (taskTree == null) return;

    //Getting the first level of tasks, because this whole structure assumes that
    //that the structure generated by my TaskScanner is the one being put in.
    Task[] currentTaskLevel = taskTree.getOutEdges();
    //System.out.println("Optimizing for task-level: " + taskTree.getID());

    //Each tasks earliest start depends on the time the largest of its dependencies takes
    for (Task t : currentTaskLevel) {

      Task[] dependencies = t.getDependencyEdges();

      //Find out which of the dependencies take the longest time
      int maxTimeDependencies = 0, earliestStartofDependency = 0;
      for (Task t2 : dependencies) {

        if (t2.getEstimatedTime() > maxTimeDependencies) {

          maxTimeDependencies = t2.getEstimatedTime();
          earliestStartofDependency = t2.getEarliestStart();

        }

      }

      t.setEarliestStart(maxTimeDependencies+earliestStartofDependency);

      optimizeTasksOnTimeOLD(t);

    }

    //Latest start depends on difference between the largest time of a task level - the tasks own time
    for (Task t : currentTaskLevel) {

      Task[] dependencies = t.getDependencyEdges();

      int maxTime = 0;
      Task taskWithMostTimeUse = null;

      for (Task t2 : dependencies) {

        if (t2.getEstimatedTime() >= maxTime) {

          maxTime = t2.getEstimatedTime();
          taskWithMostTimeUse = t2;

        }

      }

      for (Task t2 : dependencies) {

        if (t2.getID() != taskWithMostTimeUse.getID()) {

          int latestStart = (maxTime-t2.getEstimatedTime())+t2.getEarliestStart();
          if (latestStart < t2.getLatestStart()) {//If something needs the task to start earlier, then the new time is the latest a task can start

            t2.setLatestStart(latestStart);

          }

        }

      }

    }

  }

  public int findCriticalPath(Task taskTree) {

    if (taskTree == null) return 0;

    //Find a node that takes the longest time in the level
    Task[] taskLevel = taskTree.getOutEdges();
    Task criticalTask = null;
    int maxTime = 0;

    for (int i = 0; i < taskLevel.length; i++) {

      if (taskLevel[i].getEstimatedTime() >= maxTime) {

        maxTime = taskLevel[i].getEstimatedTime();
        criticalTask = taskLevel[i];

      }

    }

    if (taskLevel == null || criticalTask == null) return 0;



    criticalTask.setCritical();
    return criticalTask.getEstimatedTime() + findCriticalPath(criticalTask);

    //return joinTaskArrays(new Task[]{criticalTask},findCriticalPath(criticalTask));

  }

  public Task getTaskTree() {return this.taskTree;}

  public void printAllTasks() {

    this.taskTree.printAllInfo();
    this.taskTree.leaveAll();

  }

}
