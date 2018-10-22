package emu.jpoly.cpu;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import emu.jpoly.cpu.Framework;

/**
 * Tests various adding related instructions.
 */
public class AddTest extends Framework {

  /**
   * Add Accumulator B into index register X. The ABX instruction was included
   * in the 6809 instruction set for compatibility with the 6801 microprocessor.
   */
  @Test
  public void testABX() throws IOException {
    int instructions[] = { 0x3a // ABX
    };
    loadProg(instructions); // This will execute RESET instructions then advance to next instruction.

    setCC_A_B_DP_X_Y_S_U(0, 0, 0xCE, 0, 0x8006, 0, 0, 0);

    myTestCPU.execute();

    assertEquals(0x3a, myTestCPU.getInstructionRegister());
    assertEquals(LOCATION + 1, myTestCPU.getProgramCounter());

    chkCC_A_B_DP_X_Y_S_U(0, 0, 0xCE, 0, 0x80D4, 0, 0, 0);
  }

  @Test
  public void testADCANoCarry() {
    int instructions[] = { 0x89, // ADCA
    0x02 // value
    };
    loadProg(instructions);
    setCC_A_B_DP_X_Y_S_U(0, 5, 0, 0, 0, 0, 0, 0);

    myTestCPU.emulateCycles(2);

    assertEquals(instructions[0], myTestCPU.getInstructionRegister());
    assertEquals(LOCATION + 2, myTestCPU.getProgramCounter());

    chkCC_A_B_DP_X_Y_S_U(0, 7, 0, 0, 0, 0, 0, 0);
  }

  @Test
  public void testADCAWithCarry() {
    int instructions[] = { 0x89, // ADCA
    0x22 // value
    };
    loadProg(instructions);

    setCC_A_B_DP_X_Y_S_U(CC.Cmask, 0x14, 0, 0, 0, 0, 0, 0);

    myTestCPU.emulateCycles(2);

    assertEquals(instructions[0], myTestCPU.getInstructionRegister());
    assertEquals(LOCATION + 2, myTestCPU.getProgramCounter());

    chkCC_A_B_DP_X_Y_S_U(0, 0x37, 0, 0, 0, 0, 0, 0);
  }

  /*
   * Test that half-carry is set.
   */
  @Test
  public void testADCAWithHalfCarry() {
    int instructions[] = { 0x89, // ADCA
    0x2B // value
    };
    loadProg(instructions);

    setCC_A_B_DP_X_Y_S_U(CC.Cmask, 0x14, 0, 0, 0, 0, 0, 0);

    myTestCPU.emulateCycles(2);

    chkCC_A_B_DP_X_Y_S_U(CC.Hmask, 0x40, 0, 0, 0, 0, 0, 0);
  }

  @Test
  public void testADDB() {
    myTestCPU.setInstructionRegister(0xFFFF);

    // positive + positive with overflow
    // B=0x40 + 0x41 becomes 0x81 or -127
    myTestCPU.setCC(0);
    setB(0x40);
    myTestCPU.write(0xB00, 0xCB);
    myTestCPU.write(0xB01, 0x41);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(2);
    assertEquals(0x81, myTestCPU.getAccumulatorB());
    assertEquals(true, myTestCPU.isNegativeFlag());
    assertEquals(false, myTestCPU.isZeroFlag());
    assertEquals(true, myTestCPU.isOverflowFlag());
    assertEquals(false, myTestCPU.isCarryFlag());

    myTestCPU.setInstructionRegister(0xFFFF);

    // negative + negative
    // B=0xFF + 0xFF becomes 0xFE or -2
    myTestCPU.setCC(0);
    setB(0xFF);
    myTestCPU.write(0xB00, 0xCB);
    myTestCPU.write(0xB01, 0xFF);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(2);
    assertEquals(0xFE, myTestCPU.getAccumulatorB());
    assertEquals(true, myTestCPU.isNegativeFlag());
    assertEquals(false, myTestCPU.isZeroFlag());
    assertEquals(false, myTestCPU.isOverflowFlag());
    assertEquals(true, myTestCPU.isCarryFlag());

    myTestCPU.setInstructionRegister(0xFFFF);

    // negative + negative with overflow
    // B=0xC0 + 0xBF becomes 0x7F or 127
    myTestCPU.setCC(0);
    setB(0xC0);
    myTestCPU.write(0xB00, 0xCB);
    myTestCPU.write(0xB01, 0xBF);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(2);
    assertEquals(0x7F, myTestCPU.getAccumulatorB());
    assertEquals(false, myTestCPU.isNegativeFlag());
    assertEquals(false, myTestCPU.isZeroFlag());
    assertEquals(true, myTestCPU.isOverflowFlag());
    assertEquals(true, myTestCPU.isCarryFlag());

    myTestCPU.setInstructionRegister(0xFFFF);

    // positive + negative with negative result
    // B=0x02 + 0xFC becomes 0xFE or -2
    myTestCPU.setCC(0);
    setB(0x02);
    myTestCPU.write(0xB00, 0xCB);
    myTestCPU.write(0xB01, 0xFC);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(2);
    assertEquals(0xFE, myTestCPU.getAccumulatorB());
    assertEquals(true, myTestCPU.isNegativeFlag());
    assertEquals(false, myTestCPU.isZeroFlag());
    assertEquals(false, myTestCPU.isOverflowFlag());
    assertEquals(false, myTestCPU.isCarryFlag());
  }

  /**
   * Add 0x02 to A=0x04.
   */
  @Test
  public void testADDANoCarry() {
    int instructions[] = { 0x8B, // ADDA
    0x02, // value
    };
    loadProg(instructions);

    myTestCPU.setCarryFlag(false);
    setA(0x04);
    setB(0x05);

    myTestCPU.emulateCycles(2);

    assertEquals(0x8B, myTestCPU.getInstructionRegister());
    assertEquals(LOCATION + 2, myTestCPU.getProgramCounter());
    assertA(0x06);
    assertEquals(0x05, myTestCPU.getAccumulatorB());
    assertFalse(myTestCPU.isHalfCarryFlag());
    assertFalse(myTestCPU.isNegativeFlag());
    assertFalse(myTestCPU.isZeroFlag());
    assertFalse(myTestCPU.isOverflowFlag());
    assertFalse(myTestCPU.isCarryFlag());
  }

  /**
   * The overflow (V) bit indicates signed two’s complement overflow, which
   * occurs when the sign bit differs from the carry bit after an arithmetic
   * operation.
   */
  @Test
  public void testADDAWithCarry() {
    // A=0x03 + 0xFF becomes 0x02
    int instructions[] = { 0x8B, // ADDA
    0xFF, // value
    };
    loadProg(instructions);

    myTestCPU.setCarryFlag(false);
    setA(0x03);

    myTestCPU.emulateCycles(2);

    assertA(0x02);
    assertFalse(myTestCPU.isNegativeFlag());
    assertFalse(myTestCPU.isOverflowFlag());
    assertTrue(myTestCPU.isCarryFlag());
  }

  /**
   * Add 0x02B0 to D=0x0405 becomes 0x6B5. positive + positive = positive
   */
  @Test
  public void testADDDNoCarry() {
    int instructions[] = { 0xC3, // ADDD
    0x02, // value
    0xB0 // value
    };
    loadProg(instructions);

    myTestCPU.setCarryFlag(false);
    setA(0x04);
    setB(0x05);

    myTestCPU.emulateCycles(4);

    assertEquals(0xC3, myTestCPU.getInstructionRegister());
    assertEquals(LOCATION + 3, myTestCPU.getProgramCounter());
    assertA(0x06);
    assertEquals(0xB5, myTestCPU.getAccumulatorB());
    assertEquals(0x06B5, myTestCPU.getD());
    assertEquals(false, myTestCPU.isNegativeFlag());
    assertEquals(false, myTestCPU.isZeroFlag());
    assertEquals(false, myTestCPU.isOverflowFlag());
    assertEquals(false, myTestCPU.isCarryFlag());
  }

  /**
     */
  @Test
  public void testADDD2() {
    // Add 0xE2B0 to D=0x8405 becomes 0x66B5.
    // negative + negative = positive + overflow
    int instructions[] = { 0xC3, // ADDD
    0xE2, // value
    0xB0 // value
    };
    loadProg(instructions);
    myTestCPU.setCarryFlag(false);
    myTestCPU.setD(0x8405);
    myTestCPU.emulateCycles(4);
    assertEquals(0x66B5, myTestCPU.getD());
    assertEquals(false, myTestCPU.isNegativeFlag());
    assertEquals(false, myTestCPU.isZeroFlag());
    assertEquals(true, myTestCPU.isOverflowFlag());
    assertEquals(true, myTestCPU.isCarryFlag());

    myTestCPU.setInstructionRegister(0xFFFF);

    // negative + negative = negative
    // Add 0xE000 to D=0xD000 becomes 0xB000
    myTestCPU.setCC(0);
    myTestCPU.setD(0xD000);
    myTestCPU.write(0xB00, 0xC3);
    writeword(0xB01, 0xE000);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(4);
    assertEquals(0xB000, myTestCPU.getD());
    assertEquals(true, myTestCPU.isNegativeFlag());
    assertEquals(false, myTestCPU.isZeroFlag());
    assertEquals(false, myTestCPU.isOverflowFlag());
    assertEquals(true, myTestCPU.isCarryFlag());

    myTestCPU.setInstructionRegister(0xFFFF);

    // positive + positive = negative + overflow
    // Add 0x7000 to D=0x7000 becomes 0xE000
    myTestCPU.setCC(0);
    myTestCPU.setD(0x7000);
    myTestCPU.write(0xB00, 0xC3);
    writeword(0xB01, 0x7000);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(4);
    assertEquals(0xE000, myTestCPU.getD());
    assertEquals(true, myTestCPU.isNegativeFlag());
    assertEquals(false, myTestCPU.isZeroFlag());
    assertEquals(true, myTestCPU.isOverflowFlag());
    assertEquals(false, myTestCPU.isCarryFlag());
  }

  /**
   * Add value of SP=0x92FC to D=00C5 becomes 0x93C1. negative + positive =
   * negative
   */
  @Test
  public void testADDDstackpointer() { // INDEXED mode ,R
    int instructions[] = { 0xE3, // ADDD
    0xE4 // ,SP
    };
    loadProg(instructions);
    myTestCPU.setCarryFlag(false);
    myTestCPU.setStackPointer(0x1202);
    setA(0x00);
    setB(0xC5);
    writeword(0x1202, 0x92FC);
    myTestCPU.emulateCycles(6);
    assertEquals(LOCATION + 2, myTestCPU.getProgramCounter());
    assertEquals(0x93C1, myTestCPU.getD());
    assertEquals(true, myTestCPU.isNegativeFlag());
    assertEquals(false, myTestCPU.isZeroFlag());
    assertEquals(false, myTestCPU.isOverflowFlag());
    assertEquals(false, myTestCPU.isCarryFlag());
  }

  /**
   * Increment register A.
   */
  @Test
  public void testINCA() {
    myTestCPU.setInstructionRegister(0xFFFF);

    myTestCPU.setCC(0);
    setA(0x32);
    myTestCPU.write(0xB00, 0x4C);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(2);
    assertA(0x33);
    assertEquals(false, myTestCPU.isNegativeFlag());
    assertEquals(false, myTestCPU.isZeroFlag());
    assertEquals(false, myTestCPU.isOverflowFlag());
    assertEquals(false, myTestCPU.isCarryFlag());

    myTestCPU.setInstructionRegister(0xFFFF);

    // Test 0x7F - special case
    setA(0x7F);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(2);
    assertA(0x80);
    assertEquals(true, myTestCPU.isNegativeFlag());
    assertEquals(false, myTestCPU.isZeroFlag());
    assertEquals(true, myTestCPU.isOverflowFlag());
    assertEquals(false, myTestCPU.isCarryFlag());

    myTestCPU.setInstructionRegister(0xFFFF);

    // Test 0xFF - special case
    setA(0xFF);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(2);
    assertA(0x00);
    assertEquals(false, myTestCPU.isNegativeFlag());
    assertEquals(true, myTestCPU.isZeroFlag());
    assertEquals(false, myTestCPU.isOverflowFlag());
    assertEquals(false, myTestCPU.isCarryFlag());
  }

  /**
   * Increment memory location.
   */
  @Test
  public void testINC() {
    myTestCPU.setInstructionRegister(0xFFFF);

    myTestCPU.setCC(0);
    myTestCPU.write(0xB10, 0x7F);
    myTestCPU.write(0xB00, 0x7C);
    writeword(0xB01, 0xB10);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(7);
    assertEquals(0x80, myTestCPU.fetch(0x0B10));
    assertEquals(true, myTestCPU.isNegativeFlag());
    assertEquals(false, myTestCPU.isZeroFlag());
    assertEquals(true, myTestCPU.isOverflowFlag());
    assertEquals(false, myTestCPU.isCarryFlag());
  }
}
