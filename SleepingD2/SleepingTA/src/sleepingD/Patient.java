/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sleepingD;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author mahmo
 */
public class Patient implements Runnable{
    
    private int PatientId;
    private Room room;
    private Date inTime;

    public Patient(Room room) {
        this.room = room;
    }

    public void setInTime(Date inTime) {
        this.inTime = inTime;
    }

    public void setPatientId(int PatientId) {
        this.PatientId = PatientId;
    }

    public Date getInTime() {
        return inTime;
    }

    public int getPatientId() {
        return PatientId;
    }
    
    
    
    
    @Override
    public void run(){
        try {
            AskQuestion();
        } catch (InterruptedException ex) {
            Logger.getLogger(Patient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private synchronized void AskQuestion() throws InterruptedException {							//customer is added to the list
       
        room.EnterRoom(this);
    }
    
}
