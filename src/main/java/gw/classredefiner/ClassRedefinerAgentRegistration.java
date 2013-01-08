/*
 * Copyright 2012. Guidewire Software, Inc.
 */

package gw.classredefiner;

import com.sun.tools.attach.VirtualMachine;

import java.io.File;
import java.lang.management.ManagementFactory;

/**
 * This class is responsible for
 */
public class ClassRedefinerAgentRegistration {

  public static void registerAgentJar() {
    try {
      // We get the path to the jar by finding the URL of the source of this class (which should always be in a jar)
      String agentJarPath = new File(ClassRedefinerAgentRegistration.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath();

      String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
      int p = nameOfRunningVM.indexOf('@');
      String pid = nameOfRunningVM.substring(0, p);

      VirtualMachine vm = VirtualMachine.attach(pid);
      vm.loadAgent(agentJarPath, "");
      vm.detach();
    } catch (Exception e) {
      System.out.println("ERROR: Failed to attach to the VM");
      e.printStackTrace();
    }
  }
}
