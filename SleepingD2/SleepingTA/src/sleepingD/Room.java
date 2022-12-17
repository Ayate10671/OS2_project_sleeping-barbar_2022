
package sleepingD;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Room {
    private final ReentrantLock mutex = new ReentrantLock();
    private int waitingChairs, noOfD, availableD;
    private int TotalAnswerdQuestions, BackLaterCounter;
    private List<Patient> PatientList;
    private List<Patient> PatientBackLater;
    private Semaphore Availabe;
    private Random r = new Random();
    private Session form;

    public Room(int nChairs, int nD, int nPatient, Session form) {
        this.waitingChairs = nChairs;
        this.noOfD = nD;
        this.availableD = nD;
        this.form = form;
        Availabe = new Semaphore(availableD);
        this.PatientList = new LinkedList<Patient>();
        this.PatientBackLater = new ArrayList<Patient>(nPatient);
    }

    

    public int getTotalAnswerdQuestions() {
        return TotalAnswerdQuestions;
    }

    public int getBackLaterCounter() {
        return BackLaterCounter;
    }
    
    public void AnswerQuestion(int D_ID){
        Patient patient;
        
        
        synchronized(PatientList){
            while (PatientList.size() == 0) {
                form.SleepD(D_ID);
                System.out.println("\nD "+D_ID+" is waiting "
                		+ "for the patient and sleeps in his desk");
                try {
                    PatientList.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Room.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        
            patient = (Patient)((LinkedList<?>)PatientList).poll();
            System.out.println("Patient "+patient.getPatientId()+
            		" finds D available and Ask "
            		+ "the D "+D_ID);
        }
            int Delay;
            try {
                if (Availabe.tryAcquire() && PatientList.size() == waitingChairs){
                Availabe.acquire();
                }
                form.BusyD(D_ID);
                System.out.println("D "+D_ID+" Answer Question of "+
            		patient.getPatientId());
                
                double val = r.nextGaussian() * 2000 + 4000;				
        	Delay = Math.abs((int) Math.round(val));				
        	Thread.sleep(Delay);
                
                System.out.println("\nCompleted Answering Question of "+
        			patient.getPatientId()+" by D " + 
        			D_ID +" in "+(int)(Delay/1000)+ " seconds.");
                mutex.lock();
                try {
                    TotalAnswerdQuestions++;
                } finally {
                    mutex.unlock();
                }
                
                if (PatientList.size() > 0) {
                    System.out.println("D "+D_ID+					
            			" Calls a Patient to enter Room ");
                    form.ReturnChair(D_ID);
                }
                Availabe.release();
                
            } catch (InterruptedException e) {
            }
            
            
            
        }
        
        
    
    
    
    public void EnterRoom(Patient patient){
        System.out.println("\nPatient "+patient.getPatientId()+
        		" tries to enter Room to ask question at "
        		+patient.getInTime());
        
        synchronized(PatientList){
            if (PatientList.size() == waitingChairs) {
                
                System.out.println("\nNo chair available "
                		+ "for Patient "+patient.getPatientId()+
                		" so Patient leaves and will come back later");
                
                PatientBackLater.add(patient);
                mutex.lock();
                try {
                    BackLaterCounter++;
                } finally {
                    mutex.unlock();
                }
                return;
            }
            else if (Availabe.availablePermits() > 0 ) {
                ((LinkedList<Patient>)PatientList).offer(patient);
                PatientList.notify();
            }
            else{
                try {
                    ((LinkedList<Patient>)PatientList).offer(patient);
                    form.TakeChair();
                    System.out.println("All Ds are busy so Patient "+
                            patient.getPatientId()+
                            " takes a chair in the waiting room");
                    
                    if (PatientList.size() == 1) {
                        PatientList.notify();
                    }
                } catch (InterruptedException ex) {
                    Logger.getLogger(Room.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            
        }
    }
    
    public List<Patient> Backlater(){
        return PatientBackLater;
    }
    
    
    
}
