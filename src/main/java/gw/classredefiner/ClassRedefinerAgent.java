/*
 * Copyright 2012. Guidewire Software, Inc.
 */

package gw.classredefiner;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Field;

/**
 * The agent that is responsible for class redefinition.  This agent needs to be packaged as a jar, but otherwise
 * it will self-register when redefinedClasses() is called.
 */
public class ClassRedefinerAgent {

  // This is public, but only to aide in reflectively accessing it
  public static Instrumentation _instrumentation;

  // Used by agent initialization routines
  public static void premain(String args, Instrumentation inst) throws Exception {
    _instrumentation = inst;
  }

  // Used by agent initialization routines
  public static void agentmain(String args, Instrumentation inst) throws Exception {
    _instrumentation = inst;
  }

  /**
   * Initializes the _instrumentation variable, if necessary.  This is done in a pretty . . . interesting way.
   * The ClassRedefinerAgentRegistration class will dynamically load the jar that contains this class as a
   * Java agent, which will set _instrumentation . . . however, that could happen in a different classloader,
   * so this method will, if necessary, walk through the parent classloader hierarchy to find the version
   * of the class that was initialized and reflectively grab the _instrumentation variable off of it.
   */
  public static void initialize() {
    if (_instrumentation == null) {
      ClassRedefinerAgentRegistration.registerAgentJar();
      if (_instrumentation == null) {
        // The agent jar was loaded in a different classloader.  So yank its Instrumentation object over . . .
        Class classInRootLoader = findClassInRootLoader(ClassRedefinerAgent.class.getClassLoader());
        if (classInRootLoader == null) {
          System.out.println("ERROR: Couldn't find class in root loader!");
        } else {
          try {
            Field field = classInRootLoader.getField("_instrumentation");
            _instrumentation = (Instrumentation) field.get(null);
          } catch (Exception e) {
            System.out.println("ERROR: Instrumentation object retrieval failed");
            e.printStackTrace();
          }
        }
      }
    }
  }

  private static Class findClassInRootLoader(ClassLoader currentLoader) {
    // Basically, we want to recurse all the way up to the parent of the tree, then search in each classloader
    // starting from the top down, so that we find the loader that contains the agent that's closest to the root
    Class cls = null;
    if (currentLoader.getParent() != null) {
      cls = findClassInRootLoader(currentLoader.getParent());
    }
    if (cls != null) {
      return cls;
    } else {
      try {
        cls = Class.forName(ClassRedefinerAgent.class.getName(), false, currentLoader);
        return cls;
      } catch (ClassNotFoundException e) {
        return null;
      }
    }
  }

  /**
   * Redefines the classes passed in.  This method will throw some sort of exception if the classes are not reloaded;
   * see the docs on java.lang.instrument.Instrumentation#redefineClasses(java.lang.instrument.ClassDefinition...)
   * for more details on how this will behave.  This method will dynamically insert the appropriate agent when
   * this method is called the first time.
   *
   * @param classDefinition The ClassDefinition objects representing what should be called.
   * @see java.lang.instrument.Instrumentation#redefineClasses(java.lang.instrument.ClassDefinition...)
   */
  public static void redefineClasses(ClassDefinition... classDefinition) throws ClassNotFoundException, UnmodifiableClassException {
    initialize();
    if (_instrumentation == null) {
      throw new RuntimeException("Could not redefine classes, as the agent could not be initialized");
    } else {
      _instrumentation.redefineClasses(classDefinition);
    }
  }
}
