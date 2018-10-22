package emu.jpoly.cpu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import emu.jpoly.cpu.Framework;

public class InstructionsTest extends Framework {

//  /**
//   * Test illegal instruction 0x38.
//   */
//  @Test(expected = RuntimeException.class)
//  public void testIllegal38() {
//    myTestCPU.b.set(0xE5);
//    myTestCPU.write(0xB00, 0x38); // illegal
//    myTestCPU.pc.set(0xB00);
//    myTestCPU.execute();
//    assertEquals(0, myTestCPU.cc.getZ());
//  }
//
//  /**
//   * Test illegal instruction 0x18.
//   */
//  @Test(expected = RuntimeException.class)
//  public void testIllegal18() {
//    myTestCPU.b.set(0xE5);
//    myTestCPU.write(0xB00, 0x18); // illegal
//    myTestCPU.pc.set(0xB00);
//    myTestCPU.execute();
//    assertEquals(0, myTestCPU.cc.getZ());
//  }

  @Test
  public void testANDA() {
    setA(0x8B);
    myTestCPU.dp.set(0x0A);
    myTestCPU.cc.set(0x32);
    myTestCPU.write(0x0AEF, 0x0F);
    myTestCPU.write(0x0B00, 0x94);
    myTestCPU.write(0x0B01, 0xEF);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0x0B, myTestCPU.a.intValue());
    assertEquals(0x30, myTestCPU.cc.intValue());
  }

  @Test
  public void testANDCC() {
    myTestCPU.cc.set(0x79);
    myTestCPU.write(0x0B00, 0x1C);
    myTestCPU.write(0x0B01, 0xAF);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0x29, myTestCPU.cc.intValue());
  }

  @Test
  public void testBITimm() {
    setA(0x8B);
    myTestCPU.cc.set(0x0F);
    myTestCPU.write(0x0B00, 0x85);
    myTestCPU.write(0x0B01, 0xAA);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0x8B, myTestCPU.a.intValue());
    assertEquals(0x09, myTestCPU.cc.intValue());
    assertEquals(1, myTestCPU.cc.getN());
    assertEquals(0, myTestCPU.cc.getZ());
    assertEquals(0, myTestCPU.cc.getV());
    assertEquals(1, myTestCPU.cc.getC());
  }

  /**
   * Clear byte in extended mode.
   */
  @Test
  public void CLRMemoryByteExtended() {
    int instructions[] = { 0x7F, // CLR
    0x0F, // value
    0x23 // value
    };
    loadProg(instructions);
    setCC_A_B_DP_X_Y_S_U(0, 0, 0, 4, 0, 0, 0, 0);
    myTestCPU.write(0x0F23, 0xE2);
    myTestCPU.execute();
    assertEquals(instructions[0], myTestCPU.getInstructionRegister());
    assertEquals(LOCATION + 3, myTestCPU.pc.intValue());
    chkCC_A_B_DP_X_Y_S_U(CC.Zmask, 0, 0, 4, 0, 0, 0, 0);
    int result = myTestCPU.read(0x0F23);
    assertEquals(0, result);
  }

  /**
   * Clear accumulator.
   */
  @Test
  public void CLRAccA() {
    setA(0x8B);
    myTestCPU.cc.set(0x0F);
    myTestCPU.write(0x0B00, 0x4F);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0x0, myTestCPU.a.intValue());
    assertEquals(0, myTestCPU.cc.getN());
    assertEquals(1, myTestCPU.cc.getZ());
    assertEquals(0, myTestCPU.cc.getV());
    assertEquals(0, myTestCPU.cc.getC());
  }

  /**
   * Clear byte in direct mode with DP = 0x0B.
   */
  @Test
  public void CLRMemoryByteDirect() {
    int instructions[] = { 0x0F, // CLR
    0x23 // value
    };
    loadProg(instructions);
    setCC_A_B_DP_X_Y_S_U(0, 0, 0, 0x0B, 0, 0, 0, 0);
    myTestCPU.write(0x0B23, 0xE2);
    myTestCPU.execute();
    assertEquals(instructions[0], myTestCPU.getInstructionRegister());
    assertEquals(LOCATION + 2, myTestCPU.pc.intValue());
    chkCC_A_B_DP_X_Y_S_U(CC.Zmask, 0, 0, 0x0B, 0, 0, 0, 0);
    int result = myTestCPU.read(0x0B23);
    assertEquals(0, result);
  }

  /**
   * Clear byte in indirect mode relative to Y and increment Y.
   */
  @Test
  public void CLRMemoryByteIndexedYinc() {
    int instructions[] = { 0x6F, // CLR
    0xA0 // value
    };
    loadProg(instructions);
    setCC_A_B_DP_X_Y_S_U(CC.Nmask, 0, 0, 0, 0, 0x899, 0, 0);
    myTestCPU.write(0x0899, 0xE2);
    myTestCPU.write(0x089A, 0x22);
    myTestCPU.execute();
    assertEquals(instructions[0], myTestCPU.getInstructionRegister());
    assertEquals(LOCATION + 2, myTestCPU.pc.intValue());
    chkCC_A_B_DP_X_Y_S_U(CC.Zmask, 0, 0, 0, 0, 0x89A, 0, 0);
    int result = myTestCPU.read(0x0899);
    assertEquals(0, result);
  }

  /**
   * Complement a memory location.
   */
  @Test
  public void testCOMdirect() {
    myTestCPU.cc.clear();
    myTestCPU.dp.set(0x02);
    myTestCPU.write(0x0200, 0x07);
    // Two bytes of instruction
    myTestCPU.write(0xB00, 0x03); // COM
    myTestCPU.write(0xB01, 0x00); // Direct page addr + 0
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0xF8, myTestCPU.read(0x0200));
    assertEquals(1, myTestCPU.cc.getN());
    assertEquals(0, myTestCPU.cc.getZ());
    assertEquals(0, myTestCPU.cc.getV());
    assertEquals(1, myTestCPU.cc.getC());
  }

  /**
   * Complement register A.
   */
  @Test
  public void testCOMA() {
    myTestCPU.cc.clear();
    setA(0x74);
    myTestCPU.write(0xB00, 0x43);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0x8B, myTestCPU.a.intValue());
    assertEquals(0x09, myTestCPU.cc.intValue());
  }

  /**
   * Decimal Addition Adjust. The Half-Carry flag is not affected by this
   * instruction. The Negative flag is set equal to the new value of bit 7 in
   * Accumulator A. The Zero flag is set if the new value of Accumulator A is
   * zero; cleared otherwise. The affect this instruction has on the Overflow
   * flag is undefined. The Carry flag is set if the BCD addition produced a
   * carry; cleared otherwise.
   */
  @Test
  public void testDAA() {
    myTestCPU.write(0xB00, 0x19);
    myTestCPU.cc.clear();
    setA(0x7f);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0x85, myTestCPU.a.intValue());
    assertEquals(1, myTestCPU.cc.getN());
    assertEquals(0, myTestCPU.cc.getZ());
    assertEquals(0, myTestCPU.cc.getV());
    assertEquals(0, myTestCPU.cc.getC());
  }

  @Test
  public void testEORAindexed() {
    setY(0x12F0);
    setA(0xF2);
    myTestCPU.cc.set(0x03);
    myTestCPU.write(0x12F8, 0x98);
    myTestCPU.write(0xB00, 0xA8);
    myTestCPU.write(0xB01, 0x28);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0x98, myTestCPU.read(0x12F8));
    assertEquals(0x6A, myTestCPU.a.intValue());
    assertEquals(0x01, myTestCPU.cc.intValue());
    assertEquals(0x12F0, myTestCPU.y.intValue());
  }

  /**
   * Exchange A and DP.
   */
  @Test
  public void testEXGadp() {
    myTestCPU.cc.clear();
    setA(0x7f);
    myTestCPU.dp.set(0xf6);
    myTestCPU.write(0xB00, 0x1E);
    myTestCPU.write(0xB01, 0x8B);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0x7f, myTestCPU.dp.intValue());
    assertEquals(0xf6, myTestCPU.a.intValue());
  }

  /**
   * Exchange D and X.
   */
  @Test
  public void testEXGdx() {
    myTestCPU.write(0xB00, 0x1E);
    myTestCPU.write(0xB01, 0x01);
    myTestCPU.cc.clear();
    myTestCPU.d.set(0x117f);
    setX(0xff16);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0xff16, myTestCPU.d.intValue());
    assertEquals(0x117f, myTestCPU.x.intValue());
  }

  /**
   * Exchange A and X.
   */
  @Test
  public void testEXGax() {
    myTestCPU.write(0xB00, 0x1E);
    myTestCPU.write(0xB01, 0x81);
    myTestCPU.cc.clear();
    setA(0x56);
    setX(0x1234);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0x34, myTestCPU.a.intValue());
    assertEquals(0xFF56, myTestCPU.x.intValue());
  }

  /**
   * Increase the stack register by two. (LEAS +$ 2,S.) LEA only allows indexed
   * mode.
   * 
   */
  @Test
  public void testLEASinc2() {
    myTestCPU.s.set(0x900);
    myTestCPU.write(0xB00, 0x32); // LEAS
    myTestCPU.write(0xB01, 0x62); // SP + 2 (last 5 bits)
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0xB02, myTestCPU.pc.intValue());
    assertEquals(0x902, myTestCPU.s.intValue());
  }

  /**
   * Decrease the stack register by two. (LEAS -$ 2,S.) LEA only allows indexed
   * mode.
   * 
   */
  @Test
  public void testLEASdec2() {
    myTestCPU.s.set(0x900);
    myTestCPU.write(0xB00, 0x32); // LEAS
    myTestCPU.write(0xB01, 0x7E); // SP + 2's complement of last 5 bits.
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0xB02, myTestCPU.pc.intValue());
    assertEquals(0x8FE, myTestCPU.s.intValue());
  }

  @Test
  public void testLEAX_PCR() {
    int instructions[] = { 0x30, // LEAX >$0013,PCR
    0x8D, 0xFE, 0x49 };

    int offset = 0x10000 - 0xfe49;
    // Negate 0
    loadProg(instructions);
    myTestCPU.execute();
    assertEquals(LOCATION + 4 - offset, myTestCPU.x.intValue());
    assertEquals(LOCATION + 4, myTestCPU.pc.intValue());

    // LEAX <$93A,PCR
    myTestCPU.write(0x0846, 0x30);
    myTestCPU.write(0x0847, 0x8C);
    myTestCPU.write(0x0848, 0xF1);
    myTestCPU.pc.set(0x0846);
    myTestCPU.execute();
    assertEquals(0x0849, myTestCPU.pc.intValue());
    assertEquals(0x083a, myTestCPU.x.intValue());
  }

  /**
   * LEAX D,Y
   */
  @Test
  public void Doffset() {
    int instructions[] = { 0x30, 0xAB };

    loadProg(instructions);
    myTestCPU.cc.clear();
    setX(0xABCD);
    setY(0x804F);
    setA(0x80);
    setB(0x01);
    myTestCPU.execute();
    assertEquals(0x50, myTestCPU.x.intValue());
    assertEquals(0, myTestCPU.cc.getZ());

    myTestCPU.cc.set(0x28);
    setX(0x0EFA);
    setY(0x0EF8);
    setA(0xFF);
    setB(0x82);
    loadProg(instructions);
    myTestCPU.execute();
    assertEquals(0x0E7A, myTestCPU.x.intValue());
    assertEquals(0, myTestCPU.cc.getZ());
  }

  /**
   * Multiply 0x0C with 0x64. Result is 0x04B0. The Zero flag is set if the
   * 16-bit result is zero; cleared otherwise. The Carry flag is set equal to
   * the new value of bit 7 in Accumulator B.
   */
  @Test
  public void testMUL() {
    myTestCPU.cc.setC(0);
    myTestCPU.cc.setZ(1);
    myTestCPU.write(0xB00, 0x3D); // Write instruction into memory
    setA(0x0C);
    setB(0x64);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0x04B0, myTestCPU.d.intValue());
    assertEquals(0x04, myTestCPU.a.intValue());
    assertEquals(0xB0, myTestCPU.b.intValue());
    assertEquals(0, myTestCPU.cc.getZ());
    assertEquals(1, myTestCPU.cc.getC());
  }

  /**
   * Multiply 0x0C with 0x00. Result is 0x0000. The Zero flag is set if the
   * 16-bit result is zero; cleared otherwise. The Carry flag is set equal to
   * the new value of bit 7 in Accumulator B.
   */
  @Test
  public void testMUL0() {
    myTestCPU.cc.setC(0);
    myTestCPU.cc.setZ(1);
    myTestCPU.write(0xB00, 0x3D); // Write instruction into memory
    setA(0x0C);
    setB(0x00);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0x0000, myTestCPU.d.intValue());
    assertEquals(0x00, myTestCPU.a.intValue());
    assertEquals(0x00, myTestCPU.b.intValue());
    assertEquals(1, myTestCPU.cc.getZ());
    assertEquals(0, myTestCPU.cc.getC());
  }

  /**
   * Test the NEG - Negate instruction.
   */
  @Test
  public void testNEG() {
    // Write instruction into memory
    myTestCPU.write(0xB00, 0x40);
    // Negate 0
    setA(0);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0, myTestCPU.a.intValue());
    assertEquals(0, myTestCPU.cc.getC());

    // Negate 1
    setA(1);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0xFF, myTestCPU.a.intValue());
    assertEquals(1, myTestCPU.cc.getC());

    // Negate 2
    setA(2);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0xFE, myTestCPU.a.intValue());
    assertEquals(1, myTestCPU.cc.getC());
    assertEquals(0, myTestCPU.cc.getV());

    // Negate 0x80
    setA(0x80);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0x80, myTestCPU.a.intValue());
    assertEquals(1, myTestCPU.cc.getC());
    assertEquals(1, myTestCPU.cc.getV());
    assertEquals(0, myTestCPU.cc.getZ());
    assertEquals(1, myTestCPU.cc.getN());
  }

  @Test
  public void testNOP() {
    myTestCPU.write(0xB00, 0x12);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0xB01, myTestCPU.pc.intValue());
  }

  @Test
  public void testORAimmediate() {
    setA(0xDA);
    myTestCPU.cc.set(0x43);
    myTestCPU.write(0xB00, 0x8A);
    myTestCPU.write(0xB01, 0x0F);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0xDF, myTestCPU.a.intValue());
    assertEquals(0x49, myTestCPU.cc.intValue());
  }

  @Test
  public void testSEXlow() {
    setA(0xEE);
    setB(0x76);
    myTestCPU.write(0xB00, 0x1D);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0xB01, myTestCPU.pc.intValue());
    assertEquals(0x0076, myTestCPU.d.intValue());
  }

  @Test
  public void testSEXhigh() {
    setB(0xE6);
    myTestCPU.write(0xB00, 0x1D);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0xB01, myTestCPU.pc.intValue());
    assertEquals(0xFFE6, myTestCPU.d.intValue());
  }

  /**
   * Test SWI3.
   */
  @Test
  public void testSWI3() {
    writeword(0xfff2, 0x0300);
    myTestCPU.s.set(0x1000);
    myTestCPU.write(0xB00, 0x11);
    myTestCPU.write(0xB01, 0x3F);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0x0300, myTestCPU.pc.intValue());
    assertEquals(0x1000 - 12, myTestCPU.s.intValue());
    assertTrue(myTestCPU.cc.isSetE());
  }
  
  /**
   * Test SWI2.
   */
  @Test
  public void testSWI2() {
    writeword(0xfff4, 0x0300);
    myTestCPU.s.set(0x1000);
    myTestCPU.write(0xB00, 0x10);
    myTestCPU.write(0xB01, 0x3F);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0x0300, myTestCPU.pc.intValue());
    assertEquals(0x1000 - 12, myTestCPU.s.intValue());
    assertTrue(myTestCPU.cc.isSetE());
  }

  /**
   * Test SWI.
   */
  @Test
  public void testSWI() {
    writeword(0xfffa, 0x0300);
    myTestCPU.s.set(0x1000);
    myTestCPU.write(0xB00, 0x3F);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0x0300, myTestCPU.pc.intValue());
    assertEquals(0x1000 - 12, myTestCPU.s.intValue());
    assertTrue(myTestCPU.cc.isSetE());
    assertTrue(myTestCPU.isIrqDisableFlag());
    assertTrue(myTestCPU.isFirqDisableFlag());
  }
  
  @Test
  public void testTFR_d_y() {
    // Write instruction into memory
    myTestCPU.write(0xB00, 0x1F);
    myTestCPU.write(0xB01, 0x02);
    myTestCPU.cc.clear();
    myTestCPU.d.set(0xABBA);
    setY(0x0101);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0xB02, myTestCPU.pc.intValue());
    assertEquals(0xABBA, myTestCPU.d.intValue());
    assertEquals(0xABBA, myTestCPU.y.intValue());
    assertEquals(0, myTestCPU.cc.getN());
    assertEquals(0, myTestCPU.cc.getZ());
    assertEquals(0, myTestCPU.cc.getV());
  }

  @Test
  public void testTFR_s_pc() {
    // Write instruction into memory
    myTestCPU.write(0xB00, 0x1F);
    myTestCPU.write(0xB01, 0x45);
    myTestCPU.cc.clear();
    myTestCPU.s.set(0x1BB1);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0x1BB1, myTestCPU.pc.intValue());
    assertEquals(0x1BB1, myTestCPU.s.intValue());
    assertEquals(0, myTestCPU.cc.getN());
    assertEquals(0, myTestCPU.cc.getZ());
    assertEquals(0, myTestCPU.cc.getV());
  }

  @Test
  public void testTFR_dp_cc() {
    // Write instruction into memory
    myTestCPU.write(0xB00, 0x1F);
    myTestCPU.write(0xB01, 0xBA);
    myTestCPU.cc.clear();
    myTestCPU.dp.set(0x1B);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0x1B, myTestCPU.dp.intValue());
    assertEquals(0x1B, myTestCPU.cc.intValue());
    assertEquals(0, myTestCPU.cc.getH());
    assertEquals(1, myTestCPU.cc.getI());
    assertEquals(1, myTestCPU.cc.getN());
    assertEquals(0, myTestCPU.cc.getZ());
    assertEquals(1, myTestCPU.cc.getV());
    assertEquals(1, myTestCPU.cc.getC());
  }

  @Test
  public void testTFR_a_x() {
    myTestCPU.write(0xB00, 0x1F);
    myTestCPU.write(0xB01, 0x81);
    myTestCPU.cc.clear();
    setA(0x56);
    setB(0x78);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0x56, myTestCPU.a.intValue());
    assertEquals(0xFF56, myTestCPU.x.intValue());
  }

  @Test
  public void testTFR_x_b() {
    myTestCPU.write(0xB00, 0x1F);
    myTestCPU.write(0xB01, 0x19);
    myTestCPU.cc.clear();
    setX(0x6541);
    setB(0x78);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0x41, myTestCPU.b.intValue());
    assertEquals(0x6541, myTestCPU.x.intValue());
  }

  /**
   * Test the instruction TST. TST: The Z and N bits are affected according to
   * the value of the specified operand. The V bit is cleared.
   */
  @Test
  public void testTSTmemory() {
    // Write instruction into memory
    myTestCPU.write(0xB00, 0x4D);
    // Test a = 0xff
    myTestCPU.cc.clear();
    setA(0xff);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0xff, myTestCPU.a.intValue());
    assertEquals(1, myTestCPU.cc.getN());
    assertEquals(0, myTestCPU.cc.getZ());
    assertEquals(0, myTestCPU.cc.getV());
  }

  @Test
  public void testTSTacc01() {
    // Write instruction into memory
    myTestCPU.write(0xB00, 0x4D);
    // Test a = 0x01 and V set
    myTestCPU.cc.clear();
    myTestCPU.cc.setV(1);
    setA(0x01);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0x01, myTestCPU.a.intValue());
    assertEquals(0, myTestCPU.cc.intValue());
    assertEquals(0, myTestCPU.cc.getZ());
    assertEquals(0, myTestCPU.cc.getV());
  }

  @Test
  public void testTSTacc00() {
    // Write instruction into memory
    myTestCPU.write(0xB00, 0x4D);
    // Test a = 0x00
    myTestCPU.cc.clear();
    setA(0x00);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0, myTestCPU.cc.getN());
    assertEquals(1, myTestCPU.cc.getZ());
    assertEquals(0, myTestCPU.cc.getV());
  }

  @Test
  public void testTSTindirect() {
    // Test indirect mode: TST ,Y
    // Set up a byte to test at address 0x205
    myTestCPU.write(0x205, 0xFF);
    // Set register Y to point to that location
    setY(0x205);
    // Two bytes of instruction
    myTestCPU.write(0xB00, 0x6D);
    myTestCPU.write(0xB01, 0xA4);
    myTestCPU.pc.set(0xB00);
    myTestCPU.execute();
    assertEquals(0xFF, myTestCPU.read(0x0205));
    assertEquals(1, myTestCPU.cc.getN());
    assertEquals(0, myTestCPU.cc.getZ());
    assertEquals(0, myTestCPU.cc.getV());
  }

}
