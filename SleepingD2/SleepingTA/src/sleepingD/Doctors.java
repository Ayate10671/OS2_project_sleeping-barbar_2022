/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sleepingD;

import sleepingD.Room;

/**
 *
 * @author mahmo
 */
public class Doctors implements Runnable{
    
    private Room room;
    private int D_ID;

    public Doctors(Room room, int D_ID) {
        this.room = room;
        this.D_ID = D_ID;
    }
    
    @Override
    public void run(){
        while (true) {            
            room.AnswerQuestion(D_ID);
        }
    }
    
}
