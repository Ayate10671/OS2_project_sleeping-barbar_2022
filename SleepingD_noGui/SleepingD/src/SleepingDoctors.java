import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SleepingDoctors {
	
	public static void main (String a[]) throws InterruptedException {	
		
		int noOfDoctors=0, patientId=1, noOfPatients=0, noOfChairs;	//initializing the number of doctors and patients
		
		Scanner sc = new Scanner(System.in);
		
		System.out.println("Enter the number of Doctors:  ");			//input  doctors
    	noOfDoctors=sc.nextInt();
    	
    	System.out.println("Enter the number of waiting room"			//input  waiting chairs
    			+ " chairs:  ");
    	noOfChairs=sc.nextInt();
        
        System.out.println("Enter the number of Patients:  ");			//input  patients
    	noOfPatients=sc.nextInt();
    	

    	
		ExecutorService exec = Executors.newFixedThreadPool(12);		//initializing with 12 threads as multiple of cores in the CPU, here 6
    	Droom room = new Droom(noOfDoctors, noOfChairs);				//initializing the doctor room with the number of doctors
    	Random r = new Random();  							//a random number to calculate delays for patients arrivals and ask
       	    	
        System.out.println("\nDoctors room opened with "
        		+noOfDoctors+" doctor(s)\n");
        
        long startTime  = System.currentTimeMillis();			//start time of program
        
        for(int i=1; i<=noOfDoctors;i++) {				//generating the specified number of threads for doctor
        	
        	Doctor doctor = new Doctor(room, i);	
        	Thread thbarber = new Thread(doctor);
            exec.execute(thbarber);
        }
        
        for(int i=0;i<noOfPatients;i++) {		//patient generator; generating patient threads
        
            Patient patient = new Patient(room);
            patient.setInTime(new Date());
            Thread thpatient = new Thread(patient);
            patient.setpatientId(patientId++);
            exec.execute(thpatient);
            
            try {
            	
            	double val = r.nextGaussian() * 2000 + 2000;			//'r':object of Random class, nextGaussian() generates a number with mean 2000 and	
            	int millisDelay = Math.abs((int) Math.round(val));		//standard deviation as 2000, thus customers arrive at mean of 2000 milliseconds
            	Thread.sleep(millisDelay);					//and standard deviation of 2000 milliseconds
            }
            catch(InterruptedException iex) {
            
                iex.printStackTrace();
            }
            
        }
        
        exec.shutdown();						//shuts down the executor service and frees all the resources
        exec.awaitTermination(5, SECONDS);				//waits for 12 seconds until all the threads finish their execution
 
        long elapsedTime = System.currentTimeMillis() - startTime;	//to calculate the end time of program
        
        System.out.println("\nDoctors room closed");
        System.out.println("\nTotal time elapsed in seconds"
        		+ " for serving "+noOfPatients+" patients by "
        		+noOfDoctors+" doctors with "+noOfChairs+
        		" chairs in the waiting room is: "
        		+TimeUnit.MILLISECONDS
        	    .toSeconds(elapsedTime));
        System.out.println("\nTotal patients: "+noOfPatients+
        		"\nTotal patients asked: "+room.getTotalAskQustion()
        		+"\nTotal patients lost: "+room.getPatientLost());
               
        sc.close();
    }
}
 
class Doctor implements Runnable {		// initializing the doctor

    Droom room;
    int doctorId;
 
    public Doctor(Droom room, int doctorId) {
    
        this.room = room;
        this.doctorId = doctorId;
    }
    
    public void run() {
    
        while(true) {
        
            room.askQustion(doctorId);
        }
    }
}

class Patient implements Runnable {

    int patientId;
    Date inTime;
 
    Droom room;
 
    public Patient(Droom room) {
    
        this.room = room;
    }
 
    public int getPatientId() {			//getter and setter methods
        return patientId;
    }
 
    public Date getInTime() {
        return inTime;
    }
 
    public void setpatientId(int patientId) {
        this.patientId = patientId;
    }
 
    public void setInTime(Date inTime) {
        this.inTime = inTime;
    }
 
    public void run() {				//patient thread goes to the room for ask qustion
    
        goForAskQustion();
    }
    private synchronized void goForAskQustion() {	//patient is added to the list
    
        room.add(this);
    }

  
}
 
class Droom {

	private final AtomicInteger totalAskQustion = new AtomicInteger(0);
	private final AtomicInteger patientsLost = new AtomicInteger(0);
	int nchair, noOfDoctors, availableDoctors;
    List<Patient> listPatient;
    
    Random r = new Random();	 
    
    public Droom(int noOfDoctors, int noOfChairs){
    
        this.nchair = noOfChairs;				//number of chairs in the waiting room
        listPatient = new LinkedList<Patient>();		//list to store the arriving patients
        this.noOfDoctors = noOfDoctors;			//initializing the the total number of Doctors
        availableDoctors = noOfDoctors;
    }

    Droom(int noOfDoctors, int noOfChairs, int noOfPatients) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
 
    public AtomicInteger getTotalAskQustion() {
    	
    	totalAskQustion.get();
    	return totalAskQustion;
    }
    
    public AtomicInteger getPatientLost() {
    	
    	patientsLost.get();
    	return patientsLost;
    }
    
    public void askQustion(int doctorId)
    {
        Patient patient;
        synchronized (listPatient) {				//listPatient is a shared resource so it has been synchronized to avoid any
        							//unexpected errors in the list when multiple threads access it
            while(listPatient.size()==0) {
            
                System.out.println("\nDoctors "+doctorId+" is waiting "
                		+ "for the patient and sleeps in his chair");
                
                try {
                
                    listPatient.wait();				//doctor sleeps if there are no patients in the room
                }
                catch(InterruptedException iex) {
                
                    iex.printStackTrace();
                }
            }
            
            patient = (Patient)((LinkedList<?>)listPatient).poll();	//takes the first patient from the head of the list for ask qustion
            
            System.out.println("Patient "+patient.getPatientId()+
            		" finds the doctor asleep and wakes up "
            		+ "the doctor "+doctorId);
        }
        
        int millisDelay=0;
                
        try {
        	
        	availableDoctors--; 				//decreases the count of the available doctors as one of them starts 
        									//answer qustion of the patient and the patient leaves
            System.out.println("Doctor "+doctorId+" answer of "+
            		patient.getPatientId()+ " so patient leaves");
        	
            double val = r.nextGaussian() * 2000 + 4000;				//time taken to answer the patient's qustion has a mean of 4000 milliseconds and
        	millisDelay = Math.abs((int) Math.round(val));				//and standard deviation of 2000 milliseconds
        	Thread.sleep(millisDelay);
        	
        	System.out.println("\nFinish answer qustion of "+
        			patient.getPatientId()+" by patient " + 
        			doctorId +" in "+millisDelay+ " milliseconds.");
        
        	totalAskQustion.incrementAndGet();
            								//exits through the door
            if(listPatient.size()>0) {									
            	System.out.println("Doctor "+doctorId+			//doctor finds a sleeping patient in the waiting room, wakes him up and
            			" wakes up a patient in the "		//and then goes to his chair and sleeps until a patient arrives
            			+ "waiting room");		
            }
            
            availableDoctors++;				//doctor is available for answer qustion for the next patient
        }
        catch(InterruptedException iex) {
        
            iex.printStackTrace();
        }
        
    }
 
    public void add(Patient patient) {
    
        System.out.println("\npatient "+patient.getPatientId()+
        		" enters through the entrance door in the the clinic at "
        		+patient.getInTime());
 
        synchronized (listPatient) {
        
            if(listPatient.size() == nchair) {			//No chairs are available for the patient so the patient leaves the clinic
            
                System.out.println("\nNo chair available "
                		+ "for patient "+patient.getPatientId()+
                		" so patient leaves the clinic");
                
              patientsLost.incrementAndGet();
                
                return;
            }
            else if (availableDoctors > 0) {				//If doctor is available then the patient wakes up the doctor and sits in
            								//the chair
            	((LinkedList<Patient>)listPatient).offer(patient);
				listPatient.notify();
			}
            else {							//If doctors are busy and there are chairs in the waiting room then the patient
            								//sits on the chair in the waiting room
            	((LinkedList<Patient>)listPatient).offer(patient);
                
            	System.out.println("All barber(s) are busy so "+
            			patient.getPatientId()+
                		" takes a chair in the waiting room");
                 
                if(listPatient.size()==1)
                    listPatient.notify();
            }
        }
    }

  
}