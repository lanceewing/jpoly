package emu.jpoly.cpu;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import emu.jpoly.cpu.Framework;

public class ShiftTest extends Framework {

  /**
   * Test the ASR - Arithmic Shift Right instruction.
   */
  @Test
  public void testASR() {
    // Write instruction into memory
    myTestCPU.write(0xB00, 0x47); // ASRA
    // Logical Shift Right of 0x3E to 0x1F
    myTestCPU.cc.set(0x0F);
    setA(0x3E);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0x1F, myTestCPU.a.intValue());
    assertEquals(0x02, myTestCPU.cc.intValue());
    assertEquals(0, myTestCPU.cc.getC());
    assertEquals(0, myTestCPU.cc.getN());

    // Arithmic Shift Right of 1
    myTestCPU.cc.setC(0);
    myTestCPU.cc.setV(1);
    myTestCPU.cc.setN(1);
    setB(1);
    myTestCPU.write(0xB00, 0x57); // ASRB
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0x1F, myTestCPU.a.intValue());
    assertEquals(0x00, myTestCPU.b.intValue());
    assertEquals(1, myTestCPU.cc.getZ());
    assertEquals(1, myTestCPU.cc.getC());
    assertEquals(0, myTestCPU.cc.getN());

    // Arithmic Shift Right of 0xB8
    myTestCPU.cc.setC(0);
    myTestCPU.cc.setV(0);
    setB(0xB8);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0xDC, myTestCPU.b.intValue());
    assertEquals(0, myTestCPU.cc.getZ());
    assertEquals(0, myTestCPU.cc.getC());
    assertEquals(1, myTestCPU.cc.getN());
  }

  /**
   * Shift a byte a 0x0402, because DP = 0x04.
   */
  @Test
  public void testASRMemoryByte() {
    int instructions[] = { 0x07, // ASR
    0x02 // value
    };
    loadProg(instructions);
    setCC_A_B_DP_X_Y_S_U(0, 0, 0, 4, 0, 0, 0, 0);
    myTestCPU.write(0x0402, 0xf1);
    myTestCPU.execute();
    assertEquals(instructions[0], myTestCPU.getInstructionRegister());
    assertEquals(LOCATION + 2, myTestCPU.pc.intValue());
    chkCC_A_B_DP_X_Y_S_U(0x09, 0, 0, 4, 0, 0, 0, 0);
    int result = myTestCPU.read(0x0402);
    assertEquals(0xf8, result);
  }

  /**
   * Test the LSL - Logical Shift Left instruction. H The affect on the
   * Half-Carry flag is undefined for these instructions. N The Negative flag is
   * set equal to the new value of bit 7; previously bit 6. Z The Zero flag is
   * set if the new 8-bit value is zero; cleared otherwise. V The Overflow flag
   * is set to the Exclusive-OR of the original values of bits 6 and 7. C The
   * Carry flag receives the value shifted out of bit 7.
   */
  @Test
  public void testLSL() {
    // Write instruction into memory
    myTestCPU.write(0xB00, 0x48);
    // Logical Shift Left of 0xff in register A
    myTestCPU.cc.clear();
    setA(0xFF);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0xFE, myTestCPU.a.intValue());
    assertEquals(0x09, myTestCPU.cc.intValue());
    // assertEquals(0, myTestCPU.cc.getV());
    // assertEquals(0, myTestCPU.cc.bit.n);

    // Logical Shift Left of 1
    myTestCPU.cc.setC(0);
    myTestCPU.cc.setV(1);
    setA(1);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0x02, myTestCPU.a.intValue());
    assertEquals(0, myTestCPU.cc.intValue());
    // assertEquals(0, myTestCPU.cc.getC());
    // assertEquals(0, myTestCPU.cc.getV());

    // Logical Shift Left of 0xB8
    myTestCPU.cc.setC(0);
    myTestCPU.cc.setV(0);
    setA(0xB8);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0x70, myTestCPU.a.intValue());
    assertEquals(0x03, myTestCPU.cc.intValue());
    // assertEquals(1, myTestCPU.cc.getC());
    assertEquals(1, myTestCPU.cc.getV());
  }

  /**
   * Test the LSR - Logical Shift Right instruction.
   */
  @Test
  public void testLSR() {
    // Write instruction into memory
    myTestCPU.write(0xB00, 0x44); // LSRA
    // Logical Shift Right of 0x3E to 0x1F
    myTestCPU.cc.set(0x0F);
    setA(0x3E);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0x1F, myTestCPU.a.intValue());
    assertEquals(0x02, myTestCPU.cc.intValue());
    assertEquals(0, myTestCPU.cc.getC());
    assertEquals(0, myTestCPU.cc.getN());

    // Logical Shift Right of 1
    myTestCPU.cc.setC(0);
    myTestCPU.cc.setV(1);
    myTestCPU.cc.setN(1);
    setA(1);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0x00, myTestCPU.a.intValue());
    assertEquals(1, myTestCPU.cc.getZ());
    assertEquals(1, myTestCPU.cc.getC());
    assertEquals(0, myTestCPU.cc.getN());

    // Logical Shift Right of 0xB8
    myTestCPU.cc.setC(0);
    myTestCPU.cc.setV(0);
    setA(0xB8);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0x5C, myTestCPU.a.intValue());
    assertEquals(0, myTestCPU.cc.getZ());
    assertEquals(0, myTestCPU.cc.getC());
  }

  /**
   * Shift a byte at 0x0402, because DP = 0x04.
   */
  @Test
  public void testLSRMemoryByte() {
    int instructions[] = { 0x04, // LSR
    0x02 // value
    };
    loadProg(instructions);
    setCC_A_B_DP_X_Y_S_U(0, 0, 0, 4, 0, 0, 0, 0);
    myTestCPU.write(0x0402, 0xf1);
    myTestCPU.execute();
    assertEquals(instructions[0], myTestCPU.getInstructionRegister());
    assertEquals(LOCATION + 2, myTestCPU.pc.intValue());
    chkCC_A_B_DP_X_Y_S_U(0x01, 0, 0, 4, 0, 0, 0, 0);
    int result = myTestCPU.read(0x0402);
    assertEquals(0x78, result);
  }

  /**
   * Rotate 8-Bit Accumulator or Memory Byte Left through Carry. N The Negative
   * flag is set equal to the new value of bit 7. Z The Zero flag is set if the
   * new 8-bit value is zero; cleared otherwise. V The Overflow flag is set
   * equal to the exclusive-OR of the original values of bits 6 and 7. C The
   * Carry flag receives the value shifted out of bit 7.
   */
  @Test
  public void testROLB() {
    myTestCPU.write(0xB00, 0x59); // ROLB

    // Rotate 0x89 to 0x13.
    setB(0x89);
    myTestCPU.cc.clear();
    myTestCPU.cc.setN(1);
    myTestCPU.cc.setC(1);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0xB01, myTestCPU.pc.intValue());
    assertEquals(0x13, myTestCPU.b.intValue());
    assertEquals(0x03, myTestCPU.cc.intValue());
    assertEquals(1, myTestCPU.cc.getC());

    // Logical Shift Left of 1 with carry set
    myTestCPU.cc.setC(1);
    myTestCPU.cc.setV(1);
    setB(1);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0x03, myTestCPU.b.intValue());
    assertEquals(0, myTestCPU.cc.intValue());
    // assertEquals(0, myTestCPU.cc.getC());
    // assertEquals(0, myTestCPU.cc.getV());

    // Rotate Left of 0xD8
    myTestCPU.cc.setC(0);
    myTestCPU.cc.setV(0);
    setB(0xD8);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0xb0, myTestCPU.b.intValue());
    assertEquals(0x09, myTestCPU.cc.intValue());
    assertEquals(1, myTestCPU.cc.getC());
    assertEquals(0, myTestCPU.cc.getV());
    assertEquals(0, myTestCPU.cc.getZ());
    assertEquals(1, myTestCPU.cc.getN());
  }

  /**
   * Rotate 8-Bit Accumulator or Memory Byte Right through Carry N The Negative
   * flag is set equal to the new value of bit 7 (original value of Carry). Z
   * The Zero flag is set if the new 8-bit value is zero; cleared otherwise. V
   * The Overflow flag is not affected by these instructions. C The Carry flag
   * receives the value shifted out of bit 0.
   */
  @Test
  public void testRORB() {
    myTestCPU.write(0xB00, 0x56); // RORB
    // Rotate 0x89 with CC set to 0xC4
    setB(0x89);
    myTestCPU.cc.clear();
    myTestCPU.cc.setC(1);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0xB01, myTestCPU.pc.intValue());
    assertEquals(0xC4, myTestCPU.b.intValue());
    assertEquals(0x09, myTestCPU.cc.intValue());
    assertEquals(1, myTestCPU.cc.getN());
    assertEquals(1, myTestCPU.cc.getC());

    // Rotate 0x89 with CC clear to 0x44
    setB(0x89);
    myTestCPU.cc.clear();
    myTestCPU.cc.setC(0);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0xB01, myTestCPU.pc.intValue());
    assertEquals(0x44, myTestCPU.b.intValue());
    assertEquals(0x01, myTestCPU.cc.intValue());
    assertEquals(0, myTestCPU.cc.getN());
    assertEquals(1, myTestCPU.cc.getC());
  }

}
