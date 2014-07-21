package common;

public class SysSleep {

	 public static void sleepfixseconds (int seconds) throws Exception {
			int sleep = seconds;
			System.out.println("Sleeping " + sleep + " seconds...");
			for (int j = sleep; j > 0; j--) {
				Thread.currentThread().sleep(1000);
				System.out.print(j + " ");
			}
	 }
	 
	 public static void sleeprandomseconds (int fixseconds,int randomseconds) throws Exception {
			int sleep = fixseconds + (int)(Math.random() * randomseconds);
			System.out.println("Sleeping " + sleep + " seconds...");
			for (int j = sleep; j > 0; j--) {
				Thread.currentThread().sleep(1000);
				System.out.print(j + " ");
			}
	 } 
	
}
