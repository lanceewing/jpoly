package emu.jpoly.cpu;

import java.util.Timer;
import java.util.TimerTask;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import emu.jpoly.cpu.Framework;

public class InterruptTest extends Framework {

    private static final int IRQ_ADDR = 0xfff8;
    private static final int NMI_ADDR = 0xfffc;
    private static final int FIRQ_ADDR = 0xfff6;
  
    @Before
    public void setUp() {
        super.setUp();
        // Set register S to point to 517
        myTestCPU.s.set(517);
        myTestCPU.cc.set(0x0F);
        writeword(IRQ_ADDR, 0x1234); // Set IRQ vector
        myTestCPU.write(0x1234, 0x3B); // RTI
        myTestCPU.pc.set(0xB00);
    }


    /**
     * Test simple IRQ pushing all registers to the S stack.
     */
    @Test
    public void raiseSimpleIRQ() {
        // Set register S to point to 517
        myTestCPU.s.set(517);
        // Set the registers we want to push
        myTestCPU.cc.set(0x0F);
        setA(1);
        setB(2);
        myTestCPU.dp.set(3);
        setX(0x0405);
        setY(0x0607);
        myTestCPU.u.set(0x0809);

        writeword(IRQ_ADDR, 0x1234); // Set IRQ vector
        myTestCPU.write(0x1234, 0x3B); // RTI
        myTestCPU.pc.set(0xB00);
        myTestCPU.write(0xB00, 0x12); // NOP
        myTestCPU.write(0xB01, 0x12); // NOP
        
        myTestCPU.emulateCycle();              // Start executing first NOP. 
        myTestCPU.getBus().signalIRQ(true);    // Signal IRQ half way through execution of the NOP.
        myTestCPU.execute();                   // This will complete the first NOP. IRQ will wait until it completes.
        myTestCPU.execute();                   // Now try to execute the second NOP. IRQ steps run instead.
        
        // And we get the IRQ routine instead.
        assertEquals(0x1234, myTestCPU.pc.intValue());

        myTestCPU.getBus().signalIRQ(false);
        
        // Execute first instruction of IRQ routine, which is an RTI. 
        myTestCPU.execute();
        
        assertEquals(0xB01, myTestCPU.pc.intValue());
        assertEquals(0x8F, myTestCPU.cc.intValue()); // Flag E is set by IRQ
        assertEquals(0x01, myTestCPU.a.intValue());
        assertEquals(0x02, myTestCPU.b.intValue());
        assertEquals(517, myTestCPU.s.intValue());
        assertEquals(0x01, myTestCPU.read(517 - 1)); // Check that PC-low was pushed.
        assertEquals(0x0B, myTestCPU.read(517 - 2)); // Check that PC-high was pushed.
        assertEquals(0x09, myTestCPU.read(517 - 3)); // Check that U-low was pushed.
        assertEquals(0x08, myTestCPU.read(517 - 4)); // Check that U-high was pushed.
        assertEquals(0x07, myTestCPU.read(517 - 5)); // Check that Y-low was pushed.
        assertEquals(0x06, myTestCPU.read(517 - 6)); // Check that Y-high was pushed.
        assertEquals(0x05, myTestCPU.read(517 - 7)); // Check that X-low was pushed.
        assertEquals(0x04, myTestCPU.read(517 - 8)); // Check that X-high was pushed.
        assertEquals(0x03, myTestCPU.read(517 - 9)); // Check that DP was pushed.
        assertEquals(0x02, myTestCPU.read(517 - 10)); // Check that B was pushed.
        assertEquals(0x01, myTestCPU.read(517 - 11)); // Check that A was pushed.
        assertEquals(0x8F, myTestCPU.read(517 - 12)); // Check that CC was pushed.
    }

    /**
     * Test Non-maskable IRQ pushing all registers to the S stack.
     */
    @Test
    public void raiseNonmaskableIRQ() {
        // Set register S to point to 517
        myTestCPU.s.set(517);
        // Set the registers we want to push
        myTestCPU.cc.set(0x0F);
        setA(1);
        setB(2);
        myTestCPU.dp.set(3);
        setX(0x0405);
        setY(0x0607);
        myTestCPU.u.set(0x0809);

        writeword(NMI_ADDR, 0x1234); // Set NMI vector
        myTestCPU.write(0x1234, 0x3B); // RTI
        myTestCPU.pc.set(0xB00);
        myTestCPU.write(0xB00, 0x12); // NOP
        myTestCPU.write(0xB01, 0x12); // NOP
        
        myTestCPU.emulateCycle();              // Start executing first NOP.
        myTestCPU.getBus().signalNMI(true);    // Single NMI half way through execution of NOP.
        myTestCPU.execute();                   // This will complete the first NOP.
        myTestCPU.execute();                   // Now try to execute the second NOP.
        
        // S was not set via an instruction so the NMI was ignored (i.e. NMI was not armed)
        assertEquals(0xB02, myTestCPU.pc.intValue());

        writeword(0xB00, 0x10CE); // LDS #$900 sets CC
        writeword(0xB02, 0x900);
        myTestCPU.pc.set(0xB00);
        myTestCPU.execute();      // Execute LDS. This will arm NMI.
        myTestCPU.execute();      // Try to execute instruction after LDS. NMI executes instead.
        
        assertEquals(0x1234, myTestCPU.pc.intValue());

        myTestCPU.getBus().signalNMI(false);
        myTestCPU.execute();      // Now we execute the instruction after the LDS again.
        
        assertEquals(0xB04, myTestCPU.pc.intValue());
        assertEquals(0x81, myTestCPU.cc.intValue()); // Flag E is set by IRQ
        assertEquals(0x01, myTestCPU.a.intValue());
        assertEquals(0x02, myTestCPU.b.intValue());
        assertEquals(0x900, myTestCPU.s.intValue());
        assertEquals(0x04, myTestCPU.read(0x900 - 1)); // Check that PC-low was pushed.
        assertEquals(0x0B, myTestCPU.read(0x900 - 2)); // Check that PC-high was pushed.
        assertEquals(0x09, myTestCPU.read(0x900 - 3)); // Check that U-low was pushed.
        assertEquals(0x08, myTestCPU.read(0x900 - 4)); // Check that U-high was pushed.
        assertEquals(0x07, myTestCPU.read(0x900 - 5)); // Check that Y-low was pushed.
        assertEquals(0x06, myTestCPU.read(0x900 - 6)); // Check that Y-high was pushed.
        assertEquals(0x05, myTestCPU.read(0x900 - 7)); // Check that X-low was pushed.
        assertEquals(0x04, myTestCPU.read(0x900 - 8)); // Check that X-high was pushed.
        assertEquals(0x03, myTestCPU.read(0x900 - 9)); // Check that DP was pushed.
        assertEquals(0x02, myTestCPU.read(0x900 - 10)); // Check that B was pushed.
        assertEquals(0x01, myTestCPU.read(0x900 - 11)); // Check that A was pushed.
        assertEquals(0x81, myTestCPU.read(0x900 - 12)); // Check that CC was pushed.
    }

    /**
     * Test fast IRQ pushing CC and PC registers on stack.
     */
    @Test
    public void raiseFastIRQ() {
        // Set register S to point to 517
        myTestCPU.s.set(517);
        // Set the registers we want to push
        myTestCPU.cc.set(0x0F);
        setA(1);
        setB(2);
        myTestCPU.dp.set(3);
        setX(0x0405);
        setY(0x0607);
        myTestCPU.u.set(0x0809);

        writeword(FIRQ_ADDR, 0x1234); // Set IRQ vector
        myTestCPU.write(0x1234, 0x3B); // RTI
        myTestCPU.pc.set(0xB00);
        myTestCPU.write(0xB00, 0x12); // NOP
        myTestCPU.write(0xB01, 0x12); // NOP
        
        myTestCPU.emulateCycle();              // Start executing first NOP. 
        myTestCPU.getBus().signalFIRQ(true);   // Signal FIRQ half way through execution of the NOP.
        myTestCPU.execute();                   // This will complete the first NOP. IRQ will wait until it completes.
        myTestCPU.execute();                   // Now try to execute the second NOP. FIRQ steps run instead.
        
        // And we get the IRQ routine instead.
        assertEquals(0x1234, myTestCPU.pc.intValue());

        myTestCPU.getBus().signalFIRQ(false);
        
        // Execute first instruction of FIRQ routine, which is an RTI. 
        myTestCPU.execute();
        
        assertEquals(0xB01, myTestCPU.pc.intValue());
        assertEquals(0x0F, myTestCPU.cc.intValue()); // Flag E is off by FIRQ
        assertEquals(0x01, myTestCPU.a.intValue());
        assertEquals(0x02, myTestCPU.b.intValue());
        assertEquals(517, myTestCPU.s.intValue());
        assertEquals(0x01, myTestCPU.read(517 - 1)); // Check that PC-low was pushed.
        assertEquals(0x0B, myTestCPU.read(517 - 2)); // Check that PC-high was pushed.
        assertEquals(0x0F, myTestCPU.read(517 - 3)); // Check that CC was pushed.
    }

    /**
     * Test CWAI.
     */
    @Test
    public void callCWAI() {
        // Set register S to point to 517
        myTestCPU.s.set(517);
        // Set the registers we want to push
        myTestCPU.cc.set(0x0F);
        setA(1);
        setB(2);
        myTestCPU.dp.set(3);
        setX(0x0405);
        setY(0x0607);
        myTestCPU.u.set(0x0809);

        writeword(IRQ_ADDR, 0x1234); // Set IRQ vector
        myTestCPU.write(0x1234, 0x3B); // RTI
        myTestCPU.pc.set(0xB00);
        myTestCPU.write(0xB00, 0x3C); // CWAI
        myTestCPU.write(0xB01, 0x12); // value for CC AND
        myTestCPU.write(0xB02, 0x12); // NOP
        
        // We start executing cycles, which should keep running forever in theory,
        // because CWAI waits for an interrupt.
        for (int i=0; i<1000; i++) {
          myTestCPU.emulateCycle();
        }
        
        // Now signal the IRQ.
        myTestCPU.getBus().signalIRQ(true);
        
        // The CWAI should now complete, and PC will be pointing at IRQ vector.
        myTestCPU.execute();
        
        assertEquals(0x1234, myTestCPU.pc.intValue());
        myTestCPU.getBus().signalIRQ(false);
        
        // Execute the RTI that is at the IRQ vector address.
        myTestCPU.execute();
        
        assertEquals(0xB02, myTestCPU.pc.intValue());
        assertEquals(0x82, myTestCPU.cc.intValue()); // Flag E is set by CWAI
        assertEquals(0x02, myTestCPU.read(517 - 1)); // Check that PC-low was pushed.
        assertEquals(0x0B, myTestCPU.read(517 - 2)); // Check that PC-high was pushed.
        assertEquals(0x09, myTestCPU.read(517 - 3)); // Check that U-low was pushed.
        assertEquals(0x08, myTestCPU.read(517 - 4)); // Check that U-high was pushed.
        assertEquals(0x07, myTestCPU.read(517 - 5)); // Check that Y-low was pushed.
        assertEquals(0x06, myTestCPU.read(517 - 6)); // Check that Y-high was pushed.
        assertEquals(0x05, myTestCPU.read(517 - 7)); // Check that X-low was pushed.
        assertEquals(0x04, myTestCPU.read(517 - 8)); // Check that X-high was pushed.
        assertEquals(0x03, myTestCPU.read(517 - 9)); // Check that DP was pushed.
        assertEquals(0x02, myTestCPU.read(517 - 10)); // Check that B was pushed.
        assertEquals(0x01, myTestCPU.read(517 - 11)); // Check that A was pushed.
        assertEquals(0x82, myTestCPU.read(517 - 12)); // Check that CC was pushed.
    }

//    /**
//     * Test that ignore IRQ works.
//     */
//    @Test
//    public void ignoreIRQ() {
//        myTestCPU.cc.setI(true);
//        myTestCPU.write(0x1234, 0x12); // NOP
//        myTestCPU.write(0x1235, 0x3B); // RTI
//        myTestCPU.write(0xB00, 0x12); // NOP
//        myTestCPU.write(0xB01, 0x12); // NOP
//        myTestCPU.write(0xB02, 0x12); // NOP
//        myTestCPU.getBus().signalIRQ(true);
//        myTestCPU.execute();
//        assertEquals(0xB01, myTestCPU.pc.intValue());
//        myTestCPU.cc.setI(false); // IRQ is still raised. Should trigger now
//        myTestCPU.execute();
//        assertEquals(0x1234, myTestCPU.pc.intValue());
//        myTestCPU.execute();
//        assertEquals(0x1235, myTestCPU.pc.intValue());
//        assertTrue(myTestCPU.cc.isSetI()); // Check that I-flag is true.
//        myTestCPU.getBus().signalIRQ(false);
//        myTestCPU.execute();  // Get RTI
//        assertFalse(myTestCPU.cc.isSetI()); // Check I-flag
//    }
//
//    /**
//     * Test that IRQ is run again if it isn't lowered
//     */
//    @Test
//    public void irqNotLowered() {
//        myTestCPU.write(0x1234, 0x12); // NOP
//        myTestCPU.write(0x1235, 0x3B); // RTI
//        myTestCPU.write(0xB00, 0x12); // NOP
//        myTestCPU.write(0xB01, 0x12); // NOP
//        myTestCPU.write(0xB02, 0x12); // NOP
//        myTestCPU.getBus().signalIRQ(true);
//        myTestCPU.execute();
//        assertEquals(0x1234, myTestCPU.pc.intValue());
//        myTestCPU.execute();  // Get NOP
//        assertEquals(0x1235, myTestCPU.pc.intValue());
//        assertTrue(myTestCPU.cc.isSetI()); // Check that I-flag is true.
//        myTestCPU.execute();  // Get RTI
//        assertEquals(0x1234, myTestCPU.pc.intValue());
//        myTestCPU.execute();  // Get NOP
//        myTestCPU.getBus().signalIRQ(false);
//    }
//
//    /**
//     * Test sync.
//     * IRQ is already asserted before sync is entered, but inhibit IRQ is on.
//     */
//    @Test
//    public void sync1() {
//        // Set register S to point to 517
//        myTestCPU.s.set(517);
//        // Set the registers we want to push
//        myTestCPU.cc.set(0x5F);  // Inhibit FIRQ and IRQ
//        setA(1);
//        setB(2);
//        myTestCPU.dp.set(3);
//        setX(0x0405);
//        setY(0x0607);
//        myTestCPU.u.set(0x0809);
//
//        myTestCPU.pc.set(0xB00);
//        myTestCPU.write(0xB00, 0x13); // SYNC
//        myTestCPU.write(0xB01, 0x12); // NOP
//        myTestCPU.write(0xB02, 0x12); // NOP
//        myTestCPU.write(0xB03, 0x12); // NOP
//        myTestCPU.getBus().signalIRQ(true);
//        myTestCPU.execute();   // Execute sync
//        assertEquals(0xB01, myTestCPU.pc.intValue());
//        myTestCPU.getBus().signalIRQ(false);
//        assertEquals(0xB01, myTestCPU.pc.intValue());
//        assertEquals(0x5F, myTestCPU.cc.intValue()); // Flag E is off by FIRQ
//        assertEquals(0x01, myTestCPU.a.intValue());
//        assertEquals(0x02, myTestCPU.b.intValue());
//        assertEquals(517, myTestCPU.s.intValue());
//    }
//
//    /**
//     * Test sync.
//     * IRQ will be asserted a few milliseconds afterwards in a separate thread.
//     */
//    @Test
//    public void sync2() {
//        // Set register S to point to 517
//        myTestCPU.s.set(517);
//        // Set the registers we want to push
//        myTestCPU.cc.set(0x5F);  // Inhibit FIRQ and IRQ
//        setA(1);
//        setB(2);
//        myTestCPU.dp.set(3);
//        setX(0x0405);
//        setY(0x0607);
//        myTestCPU.u.set(0x0809);
//
//        myTestCPU.pc.set(0xB00);
//        myTestCPU.write(0xB00, 0x13); // SYNC
//        myTestCPU.write(0xB01, 0x12); // NOP
//        myTestCPU.write(0xB02, 0x12); // NOP
//        myTestCPU.write(0xB03, 0x12); // NOP
//        Timer timer = new Timer();
//        SendIRQ irqTask = new SendIRQ();
//        timer.schedule(irqTask, 100); // miliseconds
//        myTestCPU.execute();   // Execute sync
//        assertEquals(0xB01, myTestCPU.pc.intValue());
//        myTestCPU.getBus().signalIRQ(false);
//        assertEquals(0xB01, myTestCPU.pc.intValue());
//        assertEquals(0x5F, myTestCPU.cc.intValue()); // Flag E is off by FIRQ
//        assertEquals(0x01, myTestCPU.a.intValue());
//        assertEquals(0x02, myTestCPU.b.intValue());
//        assertEquals(517, myTestCPU.s.intValue());
//    }
//
//    /**
//     * Test sync without inhibit IRQ.
//     * IRQ will be asserted a few milliseconds afterwards in a separate thread.
//     */
//    @Test
//    public void sync3() {
//        // Set register S to point to 517
//        myTestCPU.s.set(517);
//        // Set the registers we want to push
//        myTestCPU.cc.set(0x4F);  // Inhibit FIRQ only
//        setA(1);
//        setB(2);
//        myTestCPU.dp.set(3);
//        setX(0x0405);
//        setY(0x0607);
//        myTestCPU.u.set(0x0809);
//
//        myTestCPU.pc.set(0xB00);
//        myTestCPU.write(0xB00, 0x13); // SYNC
//        myTestCPU.write(0xB01, 0x12); // NOP
//        myTestCPU.write(0xB02, 0x12); // NOP
//        myTestCPU.write(0xB03, 0x12); // NOP
//        Timer timer = new Timer();
//        SendIRQ irqTask = new SendIRQ();
//        timer.schedule(irqTask, 100); // miliseconds
//        myTestCPU.execute();   // Execute sync
//        assertEquals(0x1234, myTestCPU.pc.intValue());
//        myTestCPU.getBus().signalIRQ(false);
//        assertEquals(0xDF, myTestCPU.cc.intValue()); // Flags E,I,F are on while in IRQ
//        assertEquals(0x01, myTestCPU.a.intValue());
//        assertEquals(0x02, myTestCPU.b.intValue());
//        assertEquals(505, myTestCPU.s.intValue());
//        assertEquals(0x01, myTestCPU.read(517 - 1)); // Check that PC-low was pushed.
//        assertEquals(0x0B, myTestCPU.read(517 - 2)); // Check that PC-high was pushed.
//        assertEquals(0x09, myTestCPU.read(517 - 3)); // Check that U-low was pushed.
//        myTestCPU.execute();   // Execute RTI
//        assertEquals(0xB01, myTestCPU.pc.intValue());
//        assertEquals(0xCF, myTestCPU.cc.intValue());  // Flag E is on by IRQ
//        assertEquals(517, myTestCPU.s.intValue());
//    }
//
//    private class SendIRQ extends TimerTask {
//        public void run() {
//            myTestCPU.getBus().signalIRQ(true); // Execute a hardware interrupt
//        }
//    }
}
