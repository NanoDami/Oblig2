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

    optimizeTasksOnTime(taskTree);

    for (int i = 0; i < numberOfNodes; i++) {

      System.out.println("Tick: " + i);
      taskTree.tick(i);
      System.out.println();

    }

  }

  //This function prints the loop and back to the startnode 0
  public boolean isCircular(Task taskTree) {

    boolean result = false;

    if (taskTree.hasBeenVisited()) {
        System.out.print("This tree is circular at the chain:\n\t" + taskTree.getID());
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

      optimizeTasksOnTime(t);

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

  public Task[] findCriticalPath(Task taskTree) {

    if (taskTree == null) return null;

    //Find a node that takes the longest time in the level
    Task[] taskLevel = taskTree.getOutEdges();
    Task criticalTask = null;
    int maxTime = 0;

    for (int i = 0; i < taskLevel.length; i++) {

      if (taskLevel[i].getEstimatedTime() > maxTime) {

        maxTime = taskLevel[i].getEstimatedTime();
        criticalTask = taskLevel[i];

      }

    }

    return joinTaskArrays(new Task[]{criticalTask},findCriticalPath(criticalTask));

  }

  private Task[] joinTaskArrays(Task[] array1, Task[] array2) {

    Task[] newArray = Arrays.copyOf(array1, array1.length+array2.length);

    for (int i = 0; i < array2.length; i++) {

      newArray[i+array1.length] = array2[i];

    }

    return newArray;

  }

  public Task getTaskTree() {return this.taskTree;}

}
