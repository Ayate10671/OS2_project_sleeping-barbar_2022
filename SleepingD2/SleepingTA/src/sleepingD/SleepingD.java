
package sleepingD;


import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextField;


public class SleepingD {
    
    private int noOfPatients;
    private int noOfChairs;
    private int noOfD;

    public SleepingD(int noOfPatients, int noOfChairs, int noOfD) {
        this.noOfPatients = noOfPatients;
        this.noOfChairs = noOfChairs;
        this.noOfD = noOfD;
    }

  /*  SleepingD(JTextField Patients, int chairs, int D) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }*/
    
    
    
    public void Start(Session form) throws InterruptedException{
        ExecutorService exec = Executors.newFixedThreadPool(12);
        Room room = new Room(noOfChairs, noOfD, noOfPatients, form);
        Random r = new Random();
        
        System.out.println("Room is opened with "+noOfD+" Ds");
        
        long startTime  = System.currentTimeMillis();
        
        for (int i = 1; i <= noOfD; i++) {
            Doctors D = new Doctors(room, i);
            Thread thD = new Thread(D);
            exec.execute(thD);
        }
        
        for (int i = 1; i <= noOfPatients; i++) {
            try {
                Patient patient = new Patient(room);
                patient.setInTime(new Date());
                patient.setPatientId(i);
                Thread thPatient = new Thread(patient);
                exec.execute(thPatient);
                
                double val = r.nextGaussian() * 2000 + 2000;			
                int Delay = Math.abs((int) Math.round(val));		
                Thread.sleep(Delay);
                
                
            } catch (InterruptedException ex) {
                Logger.getLogger(SleepingD.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        List<Patient> backLater = room.Backlater();
        if (backLater.size() > 0 ) {
            for (int i = 0; i < backLater.size(); i++) {
            try {
                Patient patient = backLater.get(i);
                patient.setInTime(new Date());
                Thread thPatient = new Thread(patient);
                exec.execute(thPatient);
                
                double val = r.nextGaussian() * 2000 + 2000;			
                int Delay = Math.abs((int) Math.round(val));		
                Thread.sleep(Delay);
                
                
            } catch (InterruptedException ex) {
                Logger.getLogger(SleepingD.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        }
        
        exec.awaitTermination(12, SECONDS);
        exec.shutdown();
        
        long elapsedTime = (System.currentTimeMillis() - startTime)/1000;
        
        System.out.println("Room is closed");
        System.out.println("\nTotal time elapsed in seconds"
        		+ " for Answering "+noOfPatients+" patient' Questions by "
        		+noOfD+" Ds with "+noOfChairs+
        		" chairs in the waiting room is: "
        		+elapsedTime);
        System.out.println("\nTotal patients: "+noOfPatients+
        		"\nTotal patients served: "+room.getTotalAnswerdQuestions()
        		+"\nTotal patients returned: "+room.getBackLaterCounter());
    }
    
}