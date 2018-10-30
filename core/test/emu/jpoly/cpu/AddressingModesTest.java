package emu.jpoly.cpu;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests that focus solely on addressing modes and do not look at flags. This is
 * a little bit artificial, because the addressing mode code is not shared between
 * the instructions that have the same addressing mode. But they were all generated
 * by the same algorithm, so in theory their code is identical in the way that they
 * implement the addressing mode.
 */
public class AddressingModesTest extends Framework {

	@Test
	public void testImmediateMode() {
        myTestCPU.pc.set(0x500);
        myTestCPU.write(0x500, 0x86); // LDA
        myTestCPU.write(0x501, 0x20); // #$20
        
        myTestCPU.write(0x502, 0x8E); // LDX
        myTestCPU.write(0x503, 0xF0); // #$F000
        myTestCPU.write(0x504, 0x00); // 
        
        myTestCPU.write(0x505, 0x10); // LDY
        myTestCPU.write(0x506, 0x8E); // 
        myTestCPU.write(0x507, 0x00); // #$0041
        myTestCPU.write(0x508, 0x41); // 
        
        myTestCPU.emulateCycles(2);
        assertEquals(0x20, myTestCPU.getAccumulatorA());
        
        myTestCPU.emulateCycles(3);
        assertEquals(0xF000, myTestCPU.getIndexRegisterX());
        
        myTestCPU.emulateCycles(4);
        assertEquals(0x0041, myTestCPU.getIndexRegisterY());
	}
	
	@Test
	public void testExtendedMode() {
	    int instructions[] = { 
	    		0xB6,  // LDA 
	    	    0x43,  // $43A0
	    	    0xA0,
	    	    0xB7,  // STA
	    	    0x53,  // $53D0
	    	    0xD0
	    };
	    loadProg(instructions, 0x1000);
	    myTestCPU.setProgramCounter(0x1000);
	    myTestCPU.write(0x43A0, 0x0A);
	    
	    myTestCPU.emulateCycles(5);
	    assertEquals(0x0A, myTestCPU.getAccumulatorA());
	    
	    myTestCPU.emulateCycles(5);
	    assertEquals(0x0A, read(0x53D0));
	}
	
	@Test
	public void testDirectModeZeroPage() {
	    int instructions[] = { 
	    		0x96,   // LDA
	    		0xA0,   // $A0
	    		0x97,   // STA
	    		0xB0    // $B0
	    };
	    loadProg(instructions, 0x1000);
	    myTestCPU.setProgramCounter(0x1000);
	    myTestCPU.write(0xA0, 0x43);
		
	    myTestCPU.emulateCycles(4);
	    assertEquals(0x43, myTestCPU.getAccumulatorA());
	    
	    myTestCPU.emulateCycles(4);
	    assertEquals(0x43, read(0xB0));
	}
	
	@Test
	public void testDirectModeNonZeroPage() {
	    int instructions[] = {
	    		// Set up a non-zero value for the direct page register.
	    		0x86,   // LDA
	    		0xFE,   // #$FE
	    		0x1F,   // TFR
	    		0x8B,   // A,DP
	    		// Now perform the direct mode tests.
	    		0xD6,   // LDB
	    		0xC0,   // $C0
	    		0xD7,   // STB
	    		0xD0    // $D0
	    };
	    loadProg(instructions, 0x1000);
	    myTestCPU.setProgramCounter(0x1000);
	    myTestCPU.write(0xFEC0, 0x68);
	    
	    // Set value of DP first.
	    myTestCPU.emulateCycles(8);
	    assertEquals(0xFE, myTestCPU.getAccumulatorA());
	    assertEquals(0xFE, myTestCPU.getDirectPageRegister());
		
	    // Now perform the direct mode tests.
	    myTestCPU.emulateCycles(4);
	    assertEquals(0x68, myTestCPU.getAccumulatorB());
	    
	    myTestCPU.emulateCycles(4);
	    assertEquals(0x68, read(0xFED0));
	}
	
	// ,R
	@Test
	public void testIndexedNoOffsetFromRegister() {
	    int instructions[] = {
	    		0xA6,   // LDA
	    		0x84,   // ,X
	    		0xA7,   // STA
	    		0xA4,   // ,Y
	    		0xE6,   // LDB
	    		0xC4,   // ,U
	    		0xE7,   // STB
	    		0xE4    // ,S
	    };
	    loadProg(instructions, 0x1000);
	    myTestCPU.setProgramCounter(0x1000);
	    myTestCPU.setIndexRegisterX(0x1E00);
	    myTestCPU.setIndexRegisterY(0x2450);
	    myTestCPU.setUserStackPointer(0x4830);
	    myTestCPU.setStackPointer(0x7745);
	    myTestCPU.write(0x1E00, 0x43);
	    myTestCPU.write(0x4830, 0x89);
		
	    myTestCPU.emulateCycles(4);
	    assertEquals(0x43, myTestCPU.getAccumulatorA());
	    
	    myTestCPU.emulateCycles(4);
	    assertEquals(0x43, myTestCPU.read(0x2450));
	    
	    myTestCPU.emulateCycles(4);
	    assertEquals(0x89, myTestCPU.getAccumulatorB());
	    
	    myTestCPU.emulateCycles(4);
	    assertEquals(0x89, myTestCPU.read(0x7745));
	}
	
	// ,R+
	@Test
	public void testIndexedNoOffsetFromRegisterAutoIncrementBy1() {
	    int instructions[] = {
	    		0xA6,   // LDA
	    		0x80,   // ,X
	    		0xA7,   // STA
	    		0xA0,   // ,Y
	    		0xE6,   // LDB
	    		0xC0,   // ,U
	    		0xE7,   // STB
	    		0xE0    // ,S
	    };
	    loadProg(instructions, 0x1000);
	    myTestCPU.setProgramCounter(0x1000);
	    myTestCPU.setIndexRegisterX(0x1E00);
	    myTestCPU.setIndexRegisterY(0x2450);
	    myTestCPU.setUserStackPointer(0x4830);
	    myTestCPU.setStackPointer(0x7745);
	    myTestCPU.write(0x1E00, 0x43);
	    myTestCPU.write(0x4830, 0x89);
		
	    myTestCPU.emulateCycles(6);
	    assertEquals(0x43, myTestCPU.getAccumulatorA());
	    assertEquals(0x1E01, myTestCPU.getIndexRegisterX());
	    
	    myTestCPU.emulateCycles(6);
	    assertEquals(0x43, myTestCPU.read(0x2450));
	    assertEquals(0x2451, myTestCPU.getIndexRegisterY());
	    
	    myTestCPU.emulateCycles(6);
	    assertEquals(0x89, myTestCPU.getAccumulatorB());
	    assertEquals(0x4831, myTestCPU.getUserStackPointer());
	    
	    myTestCPU.emulateCycles(6);
	    assertEquals(0x89, myTestCPU.read(0x7745));
	    assertEquals(0x7746, myTestCPU.getStackPointer());
	}
	
	// ,R++
	@Test
	public void testIndexedNoOffsetFromRegisterAutoIncrementBy2() {
	    int instructions[] = {
	    		0xA6,   // LDA
	    		0x81,   // ,X
	    		0xA7,   // STA
	    		0xA1,   // ,Y
	    		0xE6,   // LDB
	    		0xC1,   // ,U
	    		0xE7,   // STB
	    		0xE1    // ,S
	    };
	    loadProg(instructions, 0x1000);
	    myTestCPU.setProgramCounter(0x1000);
	    myTestCPU.setIndexRegisterX(0x1E00);
	    myTestCPU.setIndexRegisterY(0x2450);
	    myTestCPU.setUserStackPointer(0x4830);
	    myTestCPU.setStackPointer(0x7745);
	    myTestCPU.write(0x1E00, 0x43);
	    myTestCPU.write(0x4830, 0x89);
		
	    myTestCPU.emulateCycles(7);
	    assertEquals(0x43, myTestCPU.getAccumulatorA());
	    assertEquals(0x1E02, myTestCPU.getIndexRegisterX());
	    
	    myTestCPU.emulateCycles(7);
	    assertEquals(0x43, myTestCPU.read(0x2450));
	    assertEquals(0x2452, myTestCPU.getIndexRegisterY());
	    
	    myTestCPU.emulateCycles(7);
	    assertEquals(0x89, myTestCPU.getAccumulatorB());
	    assertEquals(0x4832, myTestCPU.getUserStackPointer());
	    
	    myTestCPU.emulateCycles(7);
	    assertEquals(0x89, myTestCPU.read(0x7745));
	    assertEquals(0x7747, myTestCPU.getStackPointer());
	}
	
	// ,-R
	@Test
	public void testIndexedNoOffsetFromRegisterAutoDecrementBy1() {
	    int instructions[] = {
	    		0xA6,   // LDA
	    		0x82,   // ,X
	    		0xA7,   // STA
	    		0xA2,   // ,Y
	    		0xE6,   // LDB
	    		0xC2,   // ,U
	    		0xE7,   // STB
	    		0xE2    // ,S
	    };
	    loadProg(instructions, 0x1000);
	    myTestCPU.setProgramCounter(0x1000);
	    myTestCPU.setIndexRegisterX(0x1E00);
	    myTestCPU.setIndexRegisterY(0x2450);
	    myTestCPU.setUserStackPointer(0x4830);
	    myTestCPU.setStackPointer(0x7745);
	    myTestCPU.write(0x1DFF, 0x43);
	    myTestCPU.write(0x482F, 0x89);
		
	    myTestCPU.emulateCycles(6);
	    assertEquals(0x43, myTestCPU.getAccumulatorA());
	    assertEquals(0x1DFF, myTestCPU.getIndexRegisterX());
	    
	    myTestCPU.emulateCycles(6);
	    assertEquals(0x43, myTestCPU.read(0x244F));
	    assertEquals(0x244F, myTestCPU.getIndexRegisterY());
	    
	    myTestCPU.emulateCycles(6);
	    assertEquals(0x89, myTestCPU.getAccumulatorB());
	    assertEquals(0x482F, myTestCPU.getUserStackPointer());
	    
	    myTestCPU.emulateCycles(6);
	    assertEquals(0x89, myTestCPU.read(0x7744));
	    assertEquals(0x7744, myTestCPU.getStackPointer());
	}
	
	// ,--R
	@Test
	public void testIndexedNoOffsetFromRegisterAutoDecrementBy2() {
	    int instructions[] = {
	    		0xA6,   // LDA
	    		0x83,   // ,X
	    		0xA7,   // STA
	    		0xA3,   // ,Y
	    		0xE6,   // LDB
	    		0xC3,   // ,U
	    		0xE7,   // STB
	    		0xE3    // ,S
	    };
	    loadProg(instructions, 0x1000);
	    myTestCPU.setProgramCounter(0x1000);
	    myTestCPU.setIndexRegisterX(0x1E00);
	    myTestCPU.setIndexRegisterY(0x2450);
	    myTestCPU.setUserStackPointer(0x4830);
	    myTestCPU.setStackPointer(0x7745);
	    myTestCPU.write(0x1DFE, 0x43);
	    myTestCPU.write(0x482E, 0x89);
		
	    myTestCPU.emulateCycles(7);
	    assertEquals(0x43, myTestCPU.getAccumulatorA());
	    assertEquals(0x1DFE, myTestCPU.getIndexRegisterX());
	    
	    myTestCPU.emulateCycles(7);
	    assertEquals(0x43, myTestCPU.read(0x244E));
	    assertEquals(0x244E, myTestCPU.getIndexRegisterY());
	    
	    myTestCPU.emulateCycles(7);
	    assertEquals(0x89, myTestCPU.getAccumulatorB());
	    assertEquals(0x482E, myTestCPU.getUserStackPointer());
	    
	    myTestCPU.emulateCycles(7);
	    assertEquals(0x89, myTestCPU.read(0x7743));
	    assertEquals(0x7743, myTestCPU.getStackPointer());
	}
	
	// A,R
	@Test
	public void testIndexedAccumulatorAOffsetFromRegister() {
	    int instructions[] = {
	    		0xE6,   // LDB
	    		0x86,   // ,X
	    		0xE7,   // STB
	    		0xA6,   // ,Y
	    		0xE6,   // LDB
	    		0xC6,   // ,U
	    		0xE7,   // STB
	    		0xE6    // ,S
	    };
	    loadProg(instructions, 0x1000);
	    myTestCPU.setProgramCounter(0x1000);
	    myTestCPU.setIndexRegisterX(0x1E00);
	    myTestCPU.setIndexRegisterY(0x2450);
	    myTestCPU.setUserStackPointer(0x4830);
	    myTestCPU.setStackPointer(0x7745);
	    
	    myTestCPU.setAccumulatorA(0x50);
	    myTestCPU.write(0x1E50, 0x43);
	    myTestCPU.write(0x4880, 0x89);
		
	    myTestCPU.emulateCycles(5);
	    assertEquals(0x43, myTestCPU.getAccumulatorB());
	    
	    myTestCPU.emulateCycles(5);
	    assertEquals(0x43, myTestCPU.read(0x24A0));
	    
	    myTestCPU.emulateCycles(5);
	    assertEquals(0x89, myTestCPU.getAccumulatorB());
	    
	    myTestCPU.emulateCycles(5);
	    assertEquals(0x89, myTestCPU.read(0x7795));
	}
	
	// B,R
	@Test
	public void testIndexedAccumulatorBOffsetFromRegister() {
	    int instructions[] = {
	    		0xA6,   // LDA
	    		0x85,   // ,X
	    		0xA7,   // STA
	    		0xA5,   // ,Y
	    		0xA6,   // LDA
	    		0xC5,   // ,U
	    		0xA7,   // STA
	    		0xE5    // ,S
	    };
	    loadProg(instructions, 0x1000);
	    myTestCPU.setProgramCounter(0x1000);
	    myTestCPU.setIndexRegisterX(0x1E00);
	    myTestCPU.setIndexRegisterY(0x2450);
	    myTestCPU.setUserStackPointer(0x4830);
	    myTestCPU.setStackPointer(0x7745);
	    
	    myTestCPU.setAccumulatorB(0x50);
	    myTestCPU.write(0x1E50, 0x43);
	    myTestCPU.write(0x4880, 0x89);
		
	    myTestCPU.emulateCycles(5);
	    assertEquals(0x43, myTestCPU.getAccumulatorA());
	    
	    myTestCPU.emulateCycles(5);
	    assertEquals(0x43, myTestCPU.read(0x24A0));
	    
	    myTestCPU.emulateCycles(5);
	    assertEquals(0x89, myTestCPU.getAccumulatorA());
	    
	    myTestCPU.emulateCycles(5);
	    assertEquals(0x89, myTestCPU.read(0x7795));
	}
	
	// D,R
	@Test
	public void testIndexedAccumulatorDOffsetFromRegister() {
	    int instructions[] = {
	    		0xA6,   // LDU
	    		0x8B,   // ,X
	    		0xA7,   // STA
	    		0xAB,   // ,Y
	    		0xA6,   // LDA
	    		0xCB,   // ,U
	    		0xA7,   // STA
	    		0xEB    // ,S
	    };
	    loadProg(instructions, 0x1000);
	    myTestCPU.setProgramCounter(0x1000);
	    myTestCPU.setIndexRegisterX(0x1E00);
	    myTestCPU.setIndexRegisterY(0x2450);
	    myTestCPU.setUserStackPointer(0x4830);
	    myTestCPU.setStackPointer(0x7745);
	    myTestCPU.write(0x2E50, 0x43);
	    myTestCPU.write(0x5880, 0x89);
		
	    myTestCPU.setD(0x1050);
	    myTestCPU.emulateCycles(8);
	    assertEquals(0x43, myTestCPU.getAccumulatorA());
	    
	    myTestCPU.setD(0x1050);
	    myTestCPU.emulateCycles(8);
	    assertEquals(0x10, myTestCPU.read(0x34A0));  // Top byte of D
	    
	    myTestCPU.setD(0x1050);
	    myTestCPU.emulateCycles(8);
	    assertEquals(0x89, myTestCPU.getAccumulatorA());
	    
	    myTestCPU.setD(0x1050);
	    myTestCPU.emulateCycles(8);
	    assertEquals(0x10, myTestCPU.read(0x8795)); // Top byte of D
	}
	
	// 5n,R (positive)
	@Test
	public void testIndexed5BitOffsetFromRegister() {
	    int instructions[] = {
	    		0xA6,   // LDA
	    		0x0A,   // ,X
	    		0xA7,   // STA
	    		0x2A,   // ,Y
	    		0xE6,   // LDB
	    		0x4A,   // ,U
	    		0xE7,   // STB
	    		0x6A    // ,S
	    };
	    loadProg(instructions, 0x1000);
	    myTestCPU.setProgramCounter(0x1000);
	    myTestCPU.setIndexRegisterX(0x1E00);
	    myTestCPU.setIndexRegisterY(0x2450);
	    myTestCPU.setUserStackPointer(0x4830);
	    myTestCPU.setStackPointer(0x7745);
	    myTestCPU.write(0x1E0A, 0x43);
	    myTestCPU.write(0x483A, 0x89);
		
	    myTestCPU.emulateCycles(5);
	    assertEquals(0x43, myTestCPU.getAccumulatorA());
	    
	    myTestCPU.emulateCycles(5);
	    assertEquals(0x43, myTestCPU.read(0x245A));
	    
	    myTestCPU.emulateCycles(5);
	    assertEquals(0x89, myTestCPU.getAccumulatorB());
	    
	    myTestCPU.emulateCycles(5);
	    assertEquals(0x89, myTestCPU.read(0x774F));
	}
	
	// 5n,R (negative)
	@Test
	public void testIndexed5BitNegativeOffsetFromRegister() {
	    int instructions[] = {
	    		0xA6,   // LDA
	    		0x1A,   // ,X
	    		0xA7,   // STA
	    		0x3A,   // ,Y
	    		0xE6,   // LDB
	    		0x5A,   // ,U
	    		0xE7,   // STB
	    		0x7A    // ,S
	    };
	    loadProg(instructions, 0x1000);
	    myTestCPU.setProgramCounter(0x1000);
	    myTestCPU.setIndexRegisterX(0x1E00);
	    myTestCPU.setIndexRegisterY(0x2450);
	    myTestCPU.setUserStackPointer(0x4830);
	    myTestCPU.setStackPointer(0x7745);
	    myTestCPU.write(0x1DFA, 0x43);
	    myTestCPU.write(0x482A, 0x89);
		
	    myTestCPU.emulateCycles(5);
	    assertEquals(0x43, myTestCPU.getAccumulatorA());
	    
	    myTestCPU.emulateCycles(5);
	    assertEquals(0x43, myTestCPU.read(0x244A));
	    
	    myTestCPU.emulateCycles(5);
	    assertEquals(0x89, myTestCPU.getAccumulatorB());
	    
	    myTestCPU.emulateCycles(5);
	    assertEquals(0x89, myTestCPU.read(0x773F));
	}
	
	// 8n,R (positive)
	@Test
	public void testIndexed8BitOffsetFromRegister() {
	    int instructions[] = {
	    		0xA6,   // LDA
	    		0x88,   // ,X
	    		0x0A,
	    		0xA7,   // STA
	    		0xA8,   // ,Y
	    		0x0A,
	    		0xE6,   // LDB
	    		0xC8,   // ,U
	    		0x0A,
	    		0xE7,   // STB
	    		0xE8,   // ,S
	    		0x0A
	    };
	    loadProg(instructions, 0x1000);
	    myTestCPU.setProgramCounter(0x1000);
	    myTestCPU.setIndexRegisterX(0x1E00);
	    myTestCPU.setIndexRegisterY(0x2450);
	    myTestCPU.setUserStackPointer(0x4830);
	    myTestCPU.setStackPointer(0x7745);
	    myTestCPU.write(0x1E0A, 0x43);
	    myTestCPU.write(0x483A, 0x89);
		
	    myTestCPU.emulateCycles(5);
	    assertEquals(0x43, myTestCPU.getAccumulatorA());
	    
	    myTestCPU.emulateCycles(5);
	    assertEquals(0x43, myTestCPU.read(0x245A));
	    
	    myTestCPU.emulateCycles(5);
	    assertEquals(0x89, myTestCPU.getAccumulatorB());
	    
	    myTestCPU.emulateCycles(5);
	    assertEquals(0x89, myTestCPU.read(0x774F));
	}
	
	// 8n,R (negative)
	@Test
	public void testIndexed8BitNegativeOffsetFromRegister() {
	    int instructions[] = {
	    		0xA6,   // LDA
	    		0x88,   // ,X
	    		0xFA,
	    		0xA7,   // STA
	    		0xA8,   // ,Y
	    		0xFA,
	    		0xE6,   // LDB
	    		0xC8,   // ,U
	    		0xFA,
	    		0xE7,   // STB
	    		0xE8,   // ,S
	    		0xFA
	    };
	    loadProg(instructions, 0x1000);
	    myTestCPU.setProgramCounter(0x1000);
	    myTestCPU.setIndexRegisterX(0x1E00);
	    myTestCPU.setIndexRegisterY(0x2450);
	    myTestCPU.setUserStackPointer(0x4830);
	    myTestCPU.setStackPointer(0x7745);
	    myTestCPU.write(0x1DFA, 0x43);
	    myTestCPU.write(0x482A, 0x89);
		
	    myTestCPU.emulateCycles(5);
	    assertEquals(0x43, myTestCPU.getAccumulatorA());
	    
	    myTestCPU.emulateCycles(5);
	    assertEquals(0x43, myTestCPU.read(0x244A));
	    
	    myTestCPU.emulateCycles(5);
	    assertEquals(0x89, myTestCPU.getAccumulatorB());
	    
	    myTestCPU.emulateCycles(5);
	    assertEquals(0x89, myTestCPU.read(0x773F));
	}
	
	// 16n,R (positive)
	@Test
	public void testIndexed16BitOffsetFromRegister() {
	    int instructions[] = {
	    		0xA6,   // LDA
	    		0x89,   // ,X
	    		0x00,
	    		0x0A,
	    		0xA7,   // STA
	    		0xA9,   // ,Y
	    		0x00,
	    		0x0A,
	    		0xE6,   // LDB
	    		0xC9,   // ,U
	    		0x00,
	    		0x0A,
	    		0xE7,   // STB
	    		0xE9,   // ,S
	    		0x00,
	    		0x0A
	    };
	    loadProg(instructions, 0x1000);
	    myTestCPU.setProgramCounter(0x1000);
	    myTestCPU.setIndexRegisterX(0x1E00);
	    myTestCPU.setIndexRegisterY(0x2450);
	    myTestCPU.setUserStackPointer(0x4830);
	    myTestCPU.setStackPointer(0x7745);
	    myTestCPU.write(0x1E0A, 0x43);
	    myTestCPU.write(0x483A, 0x89);
		
	    myTestCPU.emulateCycles(8);
	    assertEquals(0x43, myTestCPU.getAccumulatorA());
	    
	    myTestCPU.emulateCycles(8);
	    assertEquals(0x43, myTestCPU.read(0x245A));
	    
	    myTestCPU.emulateCycles(8);
	    assertEquals(0x89, myTestCPU.getAccumulatorB());
	    
	    myTestCPU.emulateCycles(8);
	    assertEquals(0x89, myTestCPU.read(0x774F));
	}
	
	// 16n,R (negative)
	@Test
	public void testIndexed16BitNegativeOffsetFromRegister() {
	    int instructions[] = {
	    		0xA6,   // LDA
	    		0x89,   // ,X
	    		0xFF,
	    		0xFA,
	    		0xA7,   // STA
	    		0xA9,   // ,Y
	    		0xFF,
	    		0xFA,
	    		0xE6,   // LDB
	    		0xC9,   // ,U
	    		0xFF,
	    		0xFA,
	    		0xE7,   // STB
	    		0xE9,   // ,S
	    		0xFF,
	    		0xFA
	    };
	    loadProg(instructions, 0x1000);
	    myTestCPU.setProgramCounter(0x1000);
	    myTestCPU.setIndexRegisterX(0x1E00);
	    myTestCPU.setIndexRegisterY(0x2450);
	    myTestCPU.setUserStackPointer(0x4830);
	    myTestCPU.setStackPointer(0x7745);
	    myTestCPU.write(0x1DFA, 0x43);
	    myTestCPU.write(0x482A, 0x89);
		
	    myTestCPU.emulateCycles(8);
	    assertEquals(0x43, myTestCPU.getAccumulatorA());
	    
	    myTestCPU.emulateCycles(8);
	    assertEquals(0x43, myTestCPU.read(0x244A));
	    
	    myTestCPU.emulateCycles(8);
	    assertEquals(0x89, myTestCPU.getAccumulatorB());
	    
	    myTestCPU.emulateCycles(8);
	    assertEquals(0x89, myTestCPU.read(0x773F));
	}
	
	 // 16n,R (negative)
  @Test
  public void testIndexed16BitNegativeOffsetFromRegisterWithWrapAround() {
    // TODO: Implement.
  }
	
	// 8n,PC (positive)
	@Test
	public void testIndexed8BitOffsetFromPC() {
		// This is a strange one, because PC is changing while the program is running.
	    int instructions[] = {
	    		0xA6,   // LDA
	    		0x8C,   // ,PC
	    		0x10,
	    		0xA7,   // STA
	    		0xAC,   // ,PC
	    		0x10,
	    		0xE6,   // LDB
	    		0xCC,   // ,PC
	    		0x10,
	    		0xE7,   // STB
	    		0xEC,   // ,PC
	    		0x10
	    };
	    loadProg(instructions, 0x1000);
	    myTestCPU.setProgramCounter(0x1000);
	    myTestCPU.write(0x1013, 0x43);
	    myTestCPU.write(0x1019, 0x89);
		
	    myTestCPU.emulateCycles(5);
	    assertEquals(0x43, myTestCPU.getAccumulatorA());
	    
	    myTestCPU.emulateCycles(5);
	    assertEquals(0x43, myTestCPU.read(0x1016));
	    
	    myTestCPU.emulateCycles(5);
	    assertEquals(0x89, myTestCPU.getAccumulatorB());
	    
	    myTestCPU.emulateCycles(5);
	    assertEquals(0x89, myTestCPU.read(0x101C));
	}
	
	// 8n,PC (negative)
	@Test
	public void testIndexed8BitNegativeOffsetFromPC() {
		// This is a strange one, because PC is changing while the program is running.
	    int instructions[] = {
	    		0xA6,   // LDA
	    		0x8C,   // ,PC
	    		0xF0,
	    		0xA7,   // STA
	    		0xAC,   // ,PC
	    		0xF0,
	    		0xE6,   // LDB
	    		0xCC,   // ,PC
	    		0xF0,
	    		0xE7,   // STB
	    		0xEC,   // ,PC
	    		0xF0
	    };
	    loadProg(instructions, 0x1000);
	    myTestCPU.setProgramCounter(0x1000);
	    myTestCPU.write(0x0FF3, 0x43);
	    myTestCPU.write(0x0FF9, 0x89);
		
	    myTestCPU.emulateCycles(5);
	    assertEquals(0x43, myTestCPU.getAccumulatorA());
	    
	    myTestCPU.emulateCycles(5);
	    assertEquals(0x43, myTestCPU.read(0x0FF6));
	    
	    myTestCPU.emulateCycles(5);
	    assertEquals(0x89, myTestCPU.getAccumulatorB());
	    
	    myTestCPU.emulateCycles(5);
	    assertEquals(0x89, myTestCPU.read(0x0FFC));
	}
	
	// 16n,PC (positive)
	@Test
	public void testIndexed16BitOffsetFromPC() {
		// This is a strange one, because PC is changing while the program is running.
	    int instructions[] = {
	    		0xA6,   // LDA
	    		0x8D,   // ,PC
	    		0x00,
	    		0x10,
	    		0xA7,   // STA
	    		0xAD,   // ,PC
	    		0x00,
	    		0x10,
	    		0xE6,   // LDB
	    		0xCD,   // ,PC
	    		0x00,
	    		0x10,
	    		0xE7,   // STB
	    		0xED,   // ,PC
	    		0x00,
	    		0x10
	    };
	    loadProg(instructions, 0x1000);
	    myTestCPU.setProgramCounter(0x1000);
	    myTestCPU.write(0x1014, 0x43);
	    myTestCPU.write(0x101C, 0x89);
		
	    myTestCPU.emulateCycles(9);
	    assertEquals(0x43, myTestCPU.getAccumulatorA());
	    
	    myTestCPU.emulateCycles(9);
	    assertEquals(0x43, myTestCPU.read(0x1018));
	    
	    myTestCPU.emulateCycles(9);
	    assertEquals(0x89, myTestCPU.getAccumulatorB());
	    
	    myTestCPU.emulateCycles(9);
	    assertEquals(0x89, myTestCPU.read(0x1020));
	}
	
	// 16n,PC (positive)
	@Test
	public void testIndexed16BitNegativeOffsetFromPC() {
		// This is a strange one, because PC is changing while the program is running.
	    int instructions[] = {
	    		0xA6,   // LDA
	    		0x8D,   // ,PC
	    		0xFF,
	    		0xF0,
	    		0xA7,   // STA
	    		0xAD,   // ,PC
	    		0xFF,
	    		0xF0,
	    		0xE6,   // LDB
	    		0xCD,   // ,PC
	    		0xFF,
	    		0xF0,
	    		0xE7,   // STB
	    		0xED,   // ,PC
	    		0xFF,
	    		0xF0
	    };
	    loadProg(instructions, 0x1000);
	    myTestCPU.setProgramCounter(0x1000);
	    myTestCPU.write(0x0FF4, 0x43);
	    myTestCPU.write(0x0FFC, 0x89);
		
	    myTestCPU.emulateCycles(9);
	    assertEquals(0x43, myTestCPU.getAccumulatorA());
	    
	    myTestCPU.emulateCycles(9);
	    assertEquals(0x43, myTestCPU.read(0x0FF8));
	    
	    myTestCPU.emulateCycles(9);
	    assertEquals(0x89, myTestCPU.getAccumulatorB());
	    
	    myTestCPU.emulateCycles(9);
	    assertEquals(0x89, myTestCPU.read(0x1000));
	}
	
	// [,R]
	@Test
	public void testIndexedIndirectNoOffsetFromRegister() {
	    int instructions[] = {
	    		0xA6,   // LDA
	    		0x94,   // [0,X]
	    		0xA7,   // STA
	    		0xB4,   // [0,Y]
	    		0xE6,   // LDB
	    		0xD4,   // [0,U]
	    		0xE7,   // STB
	    		0xF4    // [0,S]
	    };
	    loadProg(instructions, 0x1000);
	    myTestCPU.setProgramCounter(0x1000);
	    myTestCPU.setIndexRegisterX(0x1E00);
	    myTestCPU.setIndexRegisterY(0x2450);
	    myTestCPU.setUserStackPointer(0x4830);
	    myTestCPU.setStackPointer(0x7745);
	    myTestCPU.write(0x1E00, 0x43);
	    myTestCPU.write(0x1E01, 0x10);
	    myTestCPU.write(0x4310, 0x32);
	    myTestCPU.write(0x2450, 0x18);
	    myTestCPU.write(0x2451, 0x80);
	    myTestCPU.write(0x4830, 0x89);
	    myTestCPU.write(0x4831, 0x20);
	    myTestCPU.write(0x8920, 0x44);
	    myTestCPU.write(0x7745, 0x50);
	    myTestCPU.write(0x7746, 0x78);
		
	    myTestCPU.emulateCycles(7);
	    assertEquals(0x32, myTestCPU.getAccumulatorA());
	    
	    myTestCPU.emulateCycles(7);
	    assertEquals(0x32, myTestCPU.read(0x1880));
	    
	    myTestCPU.emulateCycles(7);
	    assertEquals(0x44, myTestCPU.getAccumulatorB());
	    
	    myTestCPU.emulateCycles(7);
	    assertEquals(0x44, myTestCPU.read(0x5078));
	}
	
	// [,R++]
	@Test
	public void testIndexedIndirectNoOffsetFromRegisterAutoIncrementBy2() {
		
	}
	
	// [,--R]
	@Test
	public void testIndexedIndirectNoOffsetFromRegisterAutoDecrementBy2() {
		
	}
	
	// [A,R]
	@Test
	public void testIndexedIndirectAccumulatorAOffsetFromRegister() {
		
	}
	
	// [B,R]
	@Test
	public void testIndexedIndirectAccumulatorBOffsetFromRegister() {
		
	}
	
	// [D,R]
	@Test
	public void testIndexedIndirectAccumulatorDOffsetFromRegister() {
		
	}
	
	// [8n,R]
	@Test
	public void testIndexedIndirect8BitOffsetFromRegister() {
		
	}
	
	// [16n,R]
	@Test
	public void testIndexedIndirect16BitOffsetFromRegister() {
		
	}
	
	// [8n,PC]
	@Test
	public void testIndexedIndirect8BitOffsetFromPC() {
		
	}
	
	// [16n,PC]
	@Test
	public void testIndexedIndirect16BitOffsetFromPC() {
		
	}
	
	// [Addr]
	@Test
	public void testExtendedIndirect() {
	    int instructions[] = {
	    		0xA6,   // LDA
	    		0x9F,   // [000A]
	    		0x00,
	    		0x0A,
	    		0xA7,   // STA
	    		0x9F,   // [D51B]
	    		0xD5,
	    		0x1B
	    };
	    loadProg(instructions, 0x1000);
	    myTestCPU.setProgramCounter(0x1000);
	    myTestCPU.write(0x000A, 0x43);
	    myTestCPU.write(0x000B, 0x50);
	    myTestCPU.write(0x4350, 0x67);
	    myTestCPU.write(0xD51B, 0x89);
	    myTestCPU.write(0xD51C, 0x70);
		
	    myTestCPU.emulateCycles(9);
	    assertEquals(0x67, myTestCPU.getAccumulatorA());
	    
	    myTestCPU.emulateCycles(9);
	    assertEquals(0x67, myTestCPU.read(0x8970));
	}
}
