package emu.jpoly.cpu;

import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.junit.Test;

/**
 * Small test programs that combine several instructions.
 */
public class MultipleInstructionTest extends Framework {

	@Test
	public void testSumOfData() {
	    int instructions[] = {
	    	0x4F,              // CLRA
	    	0xD6, 0x41,        // LDB $41
	    	0x8E, 0x00, 0x42,  // LDX #$42
	    	0xAB, 0x80,        // ADDA ,X+
	    	0x5A,              // DECB
	    	0x26, 0xFB,        // BNE $0006
	    	0x97, 0x40         // STA $40
	    };
	    loadProg(instructions, 0x0000);
	    myTestCPU.write(0x41, 0x05);
	    myTestCPU.write(0x42, 0x15);
	    myTestCPU.write(0x43, 0x20);
	    myTestCPU.write(0x44, 0x04);
	    myTestCPU.write(0x45, 0x01);
	    myTestCPU.write(0x46, 0x34);
	    myTestCPU.setProgramCounter(0x0000);
	    while (myTestCPU.getProgramCounter() < 0x000D) {
	    	myTestCPU.execute();
	    }
	    assertEquals(0x6E, myTestCPU.read(0x0040));
	}
	
	@Test
	public void testNumberOfNegativeElements() {
	    int instructions[] = {
	    	0x8E, 0x00, 0x42,      // LDX #$42
	    	0x5F,                  // CLRB
	    	0xA6, 0x80,            // LDA ,X+
	    	0x2A, 0x01,            // BPL $0009
	    	0x5C,                  // INCB
	    	0x0A, 0x41,            // DEC $41
	    	0x26, 0xF7,            // BNE $0004
	    	0xD7, 0x40             // STB $40
	    };
	    loadProg(instructions, 0x0000);
	    myTestCPU.write(0x41, 0x05);
	    myTestCPU.write(0x42, 0x15);
	    myTestCPU.write(0x43, 0xC2);
	    myTestCPU.write(0x44, 0x04);
	    myTestCPU.write(0x45, 0x81);
	    myTestCPU.write(0x46, 0xB4);
	    myTestCPU.setProgramCounter(0x0000);
	    while (myTestCPU.getProgramCounter() < 0x000F) {
	    	myTestCPU.execute();
	    }
	    assertEquals(0x03, myTestCPU.read(0x0040));
	}
	
	@Test
	public void testMaximumValue() {
	    int instructions[] = {
    		0xD6, 0x41,           // LDB $41
    		0x4F,                 // CLRA
    		0x8E, 0x00, 0x42,     // LDX #$42
    		0xA1, 0x80,           // CMPA ,X+
    		0x24, 0x02,           // BHS $000C
    		0xA6, 0x1F,           // LDA -1,X
    		0x5A,                 // DECB
    		0x26, 0xF7,           // BNE $0006
    		0x97, 0x40            // STB $40
	    };
	    loadProg(instructions, 0x0000);
	    myTestCPU.write(0x41, 0x05);
	    myTestCPU.write(0x42, 0x67);
	    myTestCPU.write(0x43, 0x79);
	    myTestCPU.write(0x44, 0x15);
	    myTestCPU.write(0x45, 0xE3);
	    myTestCPU.write(0x46, 0x72);
	    myTestCPU.setProgramCounter(0x0000);
	    while (myTestCPU.getProgramCounter() < 0x0011) {
	    	myTestCPU.execute();
	    }
	    assertEquals(0xE3, myTestCPU.read(0x0040));
	}
	
	@Test
	public void testJustifyBinaryFraction() {
	    int instructions[] = {
    		0x5F,                    // CLRB
    		0x96, 0x40,              // LDA $40
    		0x27, 0x06,              // BEQ $000B
    		0x2B, 0x04,              // BMI $000B
    		0x5C,                    // INCB
    		0x48,                    // ASLA
    		0x20, 0xFA,              // BRA $0005
    		0xDD, 0x41               // STD $41
	    };
	    loadProg(instructions, 0x0000);
	    
	    myTestCPU.write(0x40, 0x22);
	    myTestCPU.setProgramCounter(0x0000);
	    while (myTestCPU.getProgramCounter() < 0x000D) {
	    	myTestCPU.execute();
	    }
	    assertEquals(0x88, myTestCPU.read(0x0041));
	    assertEquals(0x02, myTestCPU.read(0x0042));
	    
	    myTestCPU.write(0x40, 0x01);
	    myTestCPU.setProgramCounter(0x0000);
	    while (myTestCPU.getProgramCounter() < 0x000D) {
	    	myTestCPU.execute();
	    }
	    assertEquals(0x80, myTestCPU.read(0x0041));
	    assertEquals(0x07, myTestCPU.read(0x0042));
	    
	    myTestCPU.write(0x40, 0xCB);
	    myTestCPU.setProgramCounter(0x0000);
	    while (myTestCPU.getProgramCounter() < 0x000D) {
	    	myTestCPU.execute();
	    }
	    assertEquals(0xCB, myTestCPU.read(0x0041));
	    assertEquals(0x00, myTestCPU.read(0x0042));
	    
	    myTestCPU.write(0x40, 0x00);
	    myTestCPU.setProgramCounter(0x0000);
	    while (myTestCPU.getProgramCounter() < 0x000D) {
	    	myTestCPU.execute();
	    }
	    assertEquals(0x00, myTestCPU.read(0x0041));
	    assertEquals(0x00, myTestCPU.read(0x0042));
	}
	
	@Test
	public void testDivision() {
		// 87526245 (05378B65) / 7400 (1CE8) = 11827 (2E33)  remainder 6445 (192D)
	    int instructions[] = {
            //                              0100|           .ORG  $100
            //                              0100|                            ; sample parameters on stack ...
            0xCC, 0x8B, 0x65, //            0100|           LDD   #$....     ; dividend low word
            0x36, 0x06, //                  0103|           PSHU  d
            0xCC, 0x05, 0x37, //            0105|           LDD   #$....     ; dividend high word
            0x36, 0x06, //                  0108|           PSHU  d
            0xCC, 0x1C, 0xE8, //            010A|           LDD   #$....     ; divisor word
            0x36, 0x06, //                  010D|           PSHU  d
            0xEC, 0x42, //                  010F|   USLASH: LDD   2,u
            0xAE, 0x44, //                  0111|           LDX   4,u
            0xAF, 0x42, //                  0113|           STX   2,u
            0xED, 0x44, //                  0115|           STD   4,u
            0x68, 0x43, //                  0117|           ASL   3,u        ; initial shift of L word
            0x69, 0x42, //                  0119|           ROL   2,u
            0x8E, 0x00, 0x10, //            011B|           LDX   #$10
            0x69, 0x45, //                  011E|     USL1: ROL   5,u        ; shift H word
            0x69, 0x44, //                  0120|           ROL   4,u
            0xEC, 0x44, //                  0122|           LDD   4,u
            0xA3, 0xC4, //                  0124|           SUBD  ,u         ; does divisor fit?
            0x1C, 0xFE, //                  0126|           ANDCC #$FE       ; clc - clear carry flag
            0x2B, 0x04, //                  0128|           BMI   USL2
            0xED, 0x44, //                  012A|           STD   4,u        ; fits -> quotient = 1
            0x1A, 0x01, //                  012C|           ORCC  #$01       ; sec - Set Carry flag
            0x69, 0x43, //                  012E|     USL2: ROL   3,u        ; L word/quotient
            0x69, 0x42, //                  0130|           ROL   2,u
            0x30, 0x1F, //                  0132|           LEAX  -1,x
            0x26, 0xE8, //                  0134|           BNE   USL1
            0x33, 0x42, //                  0136|           LEAU  2,u
            0xAE, 0xC4, //                  0138|           LDX   ,u         ; quotient
            0xEC, 0x42, //                  013A|           LDD   2,u        ; remainder
	    };
	    loadProg(instructions, 0x0100);
	    myTestCPU.setProgramCounter(0x0100);
	    myTestCPU.setUserStackPointer(0x500);
	    while (myTestCPU.getProgramCounter() < 0x013C) {
	    	myTestCPU.execute();
	    }
	    assertEquals(0x2E33, myTestCPU.getIndexRegisterX());
	    assertEquals(0x192D, myTestCPU.getD());
	}

	@Test
	public void testCRC32() throws UnsupportedEncodingException {
	    int instructions[] = {
            //                              0100|           .ORG  $100
            0x10, 0xCE, 0x40, 0x00, //      0100|           LDS   #$4000
            //                              0104|    CRCHH: EQU   $ED
            //                              0104|    CRCHL: EQU   $B8
            //                              0104|    CRCLH: EQU   $83
            //                              0104|    CRCLL: EQU   $20
            //                              0104| CRCINITH: EQU   $FFFF
            //                              0104| CRCINITL: EQU   $FFFF
            //                              0104|                            ; CRC 32 bit in DP (4 bytes)
            //                              0104|      CRC: EQU   $80
            0xCE, 0x10, 0x00, //            0104|           LDU   #....      ; start address in u
            0x34, 0x10, //                  010C|           PSHS  x          ; end address +1 to TOS
            0xCC, 0xFF, 0xFF, //            010E|           LDD   #CRCINITL
            0xDD, 0x82, //                  0111|           STD   crc+2
            0x8E, 0xFF, 0xFF, //            0113|           LDX   #CRCINITH
            0x9F, 0x80, //                  0116|           STX   crc
            //                              0118|                            ; d/x contains the CRC
            //                              0118|       BL:
            0xE8, 0xC0, //                  0118|           EORB  ,u+        ; XOR with lowest byte
            0x10, 0x8E, 0x00, 0x08, //      011A|           LDY   #8         ; bit counter
            //                              011E|       RL:
            0x1E, 0x01, //                  011E|           EXG   d,x
            //                              0120|      RL1:
            0x44, //                        0120|           LSRA             ; shift CRC right, beginning with high word
            0x56, //                        0121|           RORB
            0x1E, 0x01, //                  0122|           EXG   d,x
            0x46, //                        0124|           RORA             ; low word
            0x56, //                        0125|           RORB
            0x24, 0x12, //                  0126|           BCC   cl
            //                              0128|                            ; CRC=CRC XOR polynomic
            0x88, 0x83, //                  0128|           EORA  #CRCLH     ; apply CRC polynomic low word
            0xC8, 0x20, //                  012A|           EORB  #CRCLL
            0x1E, 0x01, //                  012C|           EXG   d,x
            0x88, 0xED, //                  012E|           EORA  #CRCHH     ; apply CRC polynomic high word
            0xC8, 0xB8, //                  0130|           EORB  #CRCHL
            0x31, 0x3F, //                  0132|           LEAY  -1,y       ; bit count down
            0x26, 0xEA, //                  0134|           BNE   rl1
            0x1E, 0x01, //                  0136|           EXG   d,x        ; CRC: restore correct order
            0x27, 0x04, //                  0138|           BEQ   el         ; leave bit loop
            //                              013A|       CL:
            0x31, 0x3F, //                  013A|           LEAY  -1,y       ; bit count down
            0x26, 0xE0, //                  013C|           BNE   rl         ; bit loop
            //                              013E|       EL:
            0x11, 0xA3, 0xE4, //            013E|           CMPU  ,s         ; end address reached?
            0x26, 0xD5, //                  0141|           BNE   bl         ; byte loop
            0xDD, 0x82, //                  0143|           STD   crc+2      ; CRC low word
            0x9F, 0x80, //                  0145|           STX   crc        ; CRC high word
	    };
	    byte[] data = "ZYXWVUTSRQPONMLKJIHGFEDBCA".getBytes();
	    for (int i=0; i<data.length; i++) {
	    	myTestCPU.write(0x1000 + i, data[i]);
	    }
	    loadProg(instructions, 0x0100);
	    myTestCPU.setProgramCounter(0x0100);
	    myTestCPU.setStackPointer(0x8000);
	    myTestCPU.setIndexRegisterX(0x1000 + data.length);
	    while (myTestCPU.getProgramCounter() < 0x0147) {
	    	myTestCPU.execute();
	    }
	    
	    long crc32 = (((myTestCPU.getIndexRegisterX() * 0x10000 + myTestCPU.getD()) ^ 0xFFFFFFFF) & 0xFFFFFFFFL);
	    
	    // This Java code calculates the correct value.
		Checksum checksum = new CRC32();
		checksum.update(data, 0, data.length);
		long checksumValue = checksum.getValue();
		
	    assertEquals(0x99cdfdb2L, crc32);
	    assertEquals(checksumValue, crc32);
	}
}
