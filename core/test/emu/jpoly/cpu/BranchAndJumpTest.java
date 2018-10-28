package emu.jpoly.cpu;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import emu.jpoly.cpu.Framework;

public class BranchAndJumpTest extends Framework {

  @Test
  public void testBCC() {
    final int UNBRANCHED = 0xB00 + 2;
    final int BRANCHED = 0xB00 + 2 + 0x11;

    // Write instruction into memory
    myTestCPU.write(0xB00, 0x24); // BCC
    myTestCPU.write(0xB01, 0x11);
    myTestCPU.setCC(0);

    myTestCPU.setCarryFlag(false);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(3);
    assertEquals(BRANCHED, myTestCPU.getProgramCounter());

    myTestCPU.setCarryFlag(true);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(3);
    assertEquals(UNBRANCHED, myTestCPU.getProgramCounter());
  }

  @Test
  public void testBGEForward() {
    final int UNBRANCHED = 0xB00 + 2;
    final int BRANCHED = 0xB00 + 2 + 0x11;

    // Write instruction into memory
    myTestCPU.write(0xB00, 0x2C); // BGE
    myTestCPU.write(0xB01, 0x11);
    myTestCPU.setCC(0);

    myTestCPU.setOverflowFlag(false);
    myTestCPU.setNegativeFlag(false);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(3);
    assertEquals(BRANCHED, myTestCPU.getProgramCounter());

    myTestCPU.setOverflowFlag(true);
    myTestCPU.setNegativeFlag(false);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(3);
    assertEquals(UNBRANCHED, myTestCPU.getProgramCounter());

    myTestCPU.setOverflowFlag(false);
    myTestCPU.setNegativeFlag(true);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(3);
    assertEquals(UNBRANCHED, myTestCPU.getProgramCounter());

    myTestCPU.setOverflowFlag(true);
    myTestCPU.setNegativeFlag(true);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(3);
    assertEquals(BRANCHED, myTestCPU.getProgramCounter());
  }

  @Test
  public void testBGTForward() {
    final int UNBRANCHED = 0xB00 + 2;
    final int BRANCHED = 0xB00 + 2 + 0x11;

    // Write instruction into memory
    myTestCPU.write(0xB00, 0x2E); // BGT
    myTestCPU.write(0xB01, 0x11);
    myTestCPU.setCC(0);

    myTestCPU.setZeroFlag(false);
    myTestCPU.setOverflowFlag(false);
    myTestCPU.setNegativeFlag(false);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(3);
    assertEquals(BRANCHED, myTestCPU.getProgramCounter());

    myTestCPU.setZeroFlag(false);
    myTestCPU.setOverflowFlag(true);
    myTestCPU.setNegativeFlag(false);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(3);
    assertEquals(UNBRANCHED, myTestCPU.getProgramCounter());

    myTestCPU.setZeroFlag(false);
    myTestCPU.setOverflowFlag(false);
    myTestCPU.setNegativeFlag(true);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(3);
    assertEquals(UNBRANCHED, myTestCPU.getProgramCounter());

    myTestCPU.setZeroFlag(false);
    myTestCPU.setOverflowFlag(true);
    myTestCPU.setNegativeFlag(true);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(3);
    assertEquals(BRANCHED, myTestCPU.getProgramCounter());
  }

  /**
   * Don't branch because Z is on.
   */
  @Test
  public void testBGTWithZ() {
    int instructions[] = { 0x2E, // BGT
    17 // Jump forward 17 bytes
    };
    loadProg(instructions);
    myTestCPU.setCC(CC.Zmask);
    myTestCPU.emulateCycles(3);
    // The size of the instruction is 2 bytes.
    assertEquals(LOCATION + 2, myTestCPU.getProgramCounter());
  }

  /**
   * Branch because N and V are on.
   */
  @Test
  public void testBGTWithNandV() {
    int instructions[] = { 0x2E, // BGT
    0x17 // Jump forward 0x17 bytes
    };
    loadProg(instructions);
    myTestCPU.setCC(CC.Nmask + CC.Vmask);
    myTestCPU.emulateCycles(3);
    // The size of the instruction is 2 bytes.
    assertEquals(LOCATION + 2 + 0x17, myTestCPU.getProgramCounter());
  }

  /**
   * Branch because Z, N and V are off.
   */
  @Test
  public void testBGTWithNandVoff() {
    int instructions[] = { 0x2E, // BGT
    0x17 // Jump forward 0x17 bytes
    };
    loadProg(instructions);
    myTestCPU.setCC(CC.Cmask);
    myTestCPU.emulateCycles(3);
    // The size of the instruction is 2 bytes.
    assertEquals(LOCATION + 2 + 0x17, myTestCPU.getProgramCounter());
  }

  @Test
  public void testBHI() {
    final int UNBRANCHED = 0xB00 + 2;
    final int BRANCHED = 0xB00 + 2 + 0x11;

    // Write instruction into memory
    myTestCPU.write(0xB00, 0x22); // BHI
    myTestCPU.write(0xB01, 0x11);
    myTestCPU.setCC(0);

    myTestCPU.setCarryFlag(false);
    myTestCPU.setZeroFlag(false);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(3);
    assertEquals(BRANCHED, myTestCPU.getProgramCounter());

    myTestCPU.setCarryFlag(true);
    myTestCPU.setZeroFlag(false);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(3);
    assertEquals(UNBRANCHED, myTestCPU.getProgramCounter());

    myTestCPU.setCarryFlag(false);
    myTestCPU.setZeroFlag(true);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(3);
    assertEquals(UNBRANCHED, myTestCPU.getProgramCounter());

    myTestCPU.setCarryFlag(true);
    myTestCPU.setZeroFlag(true);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(3);
    assertEquals(UNBRANCHED, myTestCPU.getProgramCounter());
  }

  @Test
  public void testBLE() {
    final int UNBRANCHED = 0xB00 + 2;
    final int BRANCHED = 0xB00 + 2 + 0x11;

    // Write instruction into memory
    myTestCPU.write(0xB00, 0x2F); // BLE
    myTestCPU.write(0xB01, 0x11);
    myTestCPU.setCC(0);

    myTestCPU.setZeroFlag(false);
    myTestCPU.setOverflowFlag(false);
    myTestCPU.setNegativeFlag(false);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(3);
    assertEquals(UNBRANCHED, myTestCPU.getProgramCounter());

    myTestCPU.setOverflowFlag(true);
    myTestCPU.setNegativeFlag(false);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(3);
    assertEquals(BRANCHED, myTestCPU.getProgramCounter());

    myTestCPU.setOverflowFlag(false);
    myTestCPU.setNegativeFlag(true);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(3);
    assertEquals(BRANCHED, myTestCPU.getProgramCounter());

    myTestCPU.setOverflowFlag(true);
    myTestCPU.setNegativeFlag(true);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(3);
    assertEquals(UNBRANCHED, myTestCPU.getProgramCounter());

    myTestCPU.setZeroFlag(true);
    myTestCPU.setOverflowFlag(false);
    myTestCPU.setNegativeFlag(false);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(3);
    assertEquals(BRANCHED, myTestCPU.getProgramCounter());
  }

  @Test
  public void testBLS() {
    final int UNBRANCHED = 0xB00 + 2;
    final int BRANCHED = 0xB00 + 2 + 0x11;

    // Write instruction into memory
    myTestCPU.write(0xB00, 0x23); // BLS
    myTestCPU.write(0xB01, 0x11);
    myTestCPU.setCC(0);

    myTestCPU.setZeroFlag(false);
    myTestCPU.setCarryFlag(false);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(3);
    assertEquals(UNBRANCHED, myTestCPU.getProgramCounter());

    myTestCPU.setZeroFlag(true);
    myTestCPU.setCarryFlag(false);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(3);
    assertEquals(BRANCHED, myTestCPU.getProgramCounter());

    myTestCPU.setZeroFlag(false);
    myTestCPU.setCarryFlag(true);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(3);
    assertEquals(BRANCHED, myTestCPU.getProgramCounter());

    myTestCPU.setZeroFlag(true);
    myTestCPU.setCarryFlag(true);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(3);
    assertEquals(BRANCHED, myTestCPU.getProgramCounter());
  }

  @Test
  public void testBLT() {
    final int UNBRANCHED = 0xB00 + 2;
    final int BRANCHED = 0xB00 + 2 + 0x11;

    // Write instruction into memory
    myTestCPU.write(0xB00, 0x2D); // BLT
    myTestCPU.write(0xB01, 0x11);
    myTestCPU.setCC(0);

    myTestCPU.setOverflowFlag(false);
    myTestCPU.setNegativeFlag(false);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(3);
    assertEquals(UNBRANCHED, myTestCPU.getProgramCounter());

    myTestCPU.setOverflowFlag(true);
    myTestCPU.setNegativeFlag(false);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(3);
    assertEquals(BRANCHED, myTestCPU.getProgramCounter());

    myTestCPU.setOverflowFlag(false);
    myTestCPU.setNegativeFlag(true);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(3);
    assertEquals(BRANCHED, myTestCPU.getProgramCounter());

    myTestCPU.setOverflowFlag(true);
    myTestCPU.setNegativeFlag(true);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(3);
    assertEquals(UNBRANCHED, myTestCPU.getProgramCounter());
  }

  @Test
  public void testBRAForward() {
    int instructions[] = { 0x20, // BRA
    17 // Jump forward 17 bytes
    };
    loadProg(instructions);
    myTestCPU.emulateCycles(3);
    // The size of the instruction is 2 bytes.
    assertEquals(LOCATION + 2 + 17, myTestCPU.getProgramCounter());
  }

  @Test
  public void testBRABackward() {
    int instructions[] = { 0x20, // BRA
    170 // Jump backward 170 - 256 = 86 bytes
    };
    loadProg(instructions);
    myTestCPU.emulateCycles(3);
    // The size of the instruction is 2 bytes.
    assertEquals(LOCATION + 2 - 86, myTestCPU.getProgramCounter());
  }

  @Test
  public void testBSRBackward() {
    int instructions[] = { 0x8d, // BSR
    170 // Jump backward 170 - 256 = 86 bytes
    };
    loadProg(instructions);
    myTestCPU.setStackPointer(0x300);
    myTestCPU.emulateCycles(7);
    // The size of the instruction is 2 bytes.
    assertEquals(LOCATION + 2 - 86, myTestCPU.getProgramCounter());
    assertEquals(0x2fe, myTestCPU.getStackPointer());
    assertEquals(0x22, myTestCPU.fetch(0x2ff));
    assertEquals(0x1e, myTestCPU.fetch(0x2fe));
  }

  @Test
  public void testBSRForward() {
    int instructions[] = { 0x8d, // BSR
    17 // Jump forward 17 bytes
    };
    loadProg(instructions);
    myTestCPU.setStackPointer(0x300);
    myTestCPU.emulateCycles(7);
    // The size of the instruction is 2 bytes.
    assertEquals(LOCATION + 2 + 17, myTestCPU.getProgramCounter());
    assertEquals(0x2fe, myTestCPU.getStackPointer());
    assertEquals(0x22, myTestCPU.fetch(0x2ff));
    assertEquals(0x1e, myTestCPU.fetch(0x2fe));
  }

  @Test
  public void testLBSRbackward() {
    final int STACKADDR = 0x300;
    int instructions[] = { 0x17, // LBSR
    0xF8, 0xD5 };
    loadProg(instructions);
    myTestCPU.setStackPointer(STACKADDR);
    myTestCPU.emulateCycles(9);
    // The size of the instruction is 2 bytes.
    assertEquals(LOCATION + 3 - 0x072B, myTestCPU.getProgramCounter());
    assertEquals(STACKADDR - 2, myTestCPU.getStackPointer());
    assertEquals(LOCATION + 3, read_word(STACKADDR - 2));
  }

  @Test
  public void testLBSRforward() {
    final int STACKADDR = 0x300;
    int instructions[] = { 0x17, // LBSR
    0x03, 0x72 };
    loadProg(instructions);
    myTestCPU.setStackPointer(STACKADDR);
    myTestCPU.emulateCycles(9);
    // The size of the instruction is 2 bytes.
    assertEquals(LOCATION + 3 + 0x0372, myTestCPU.getProgramCounter());
    assertEquals(STACKADDR - 2, myTestCPU.getStackPointer());
    assertEquals(LOCATION + 3, read_word(STACKADDR - 2));
  }

  /**
   * Test the JSR - Jump to Subroutine - instruction. INDEXED mode: JSR D,Y
   */
  @Test
  public void testJSR() {
    // Set up a word to test at address 0x205
    writeword(0x205, 0x03ff);
    // Set register D
    myTestCPU.setD(0x105);
    // Set register Y to point to that location minus 5
    setY(0x200);
    // Set register S to point to 0x915
    myTestCPU.setStackPointer(0x915);
    // Two bytes of instruction
    myTestCPU.write(0xB00, 0xAD); // JSR D,Y
    myTestCPU.write(0xB01, 0xAB);
    myTestCPU.write(0xB02, 0x11); // Junk
    myTestCPU.write(0xB03, 0x22); // Junk
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.setCC(0);
    myTestCPU.emulateCycles(11);
    chkCC_A_B_DP_X_Y_S_U(0, 0x01, 0x05, 0, 0, 0x200, 0x913, 0);
    assertEquals(0x200, myTestCPU.getIndexRegisterY());
    assertEquals(0x105, myTestCPU.getD());
    assertEquals(0x913, myTestCPU.getStackPointer());
    assertEquals(0x305, myTestCPU.getProgramCounter());
  }

  @Test
  public void testLBRAForwards() {
    myTestCPU.write(0xB00, 0x16);
    writeword(0xB01, 0x03FF);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(5);
    assertEquals(0xB00 + 3 + 0x03FF, myTestCPU.getProgramCounter());
  }

  @Test
  public void testLBRABackwards() {
    myTestCPU.write(0x1B00, 0x16);
    writeword(0x1B01, 0xF333);
    myTestCPU.setProgramCounter(0x1B00);
    myTestCPU.emulateCycles(5);
    assertEquals(0x1B00 + 3 - 0xCCD, myTestCPU.getProgramCounter());
  }

  @Test
  public void testLBRNForwards() {
    writeword(0xB00, 0x1021);
    writeword(0xB02, 0x03FF);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(5);
    assertEquals(0xB00 + 4, myTestCPU.getProgramCounter());
  }

  @Test
  public void testLBCCForwards() {
    myTestCPU.setCarryFlag(false);
    writeword(0xB00, 0x1024);
    writeword(0xB02, 0x03FF);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(5);
    assertEquals(0xB00 + 4 + 0x03FF, myTestCPU.getProgramCounter());
  }

  @Test
  public void testLBEQForwards() {
    myTestCPU.setZeroFlag(true);
    writeword(0xB00, 0x1027);
    writeword(0xB02, 0x03FF);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(5);
    assertEquals(0xB00 + 4 + 0x03FF, myTestCPU.getProgramCounter());
  }

  @Test
  public void testLBGEForwards() {
    myTestCPU.setNegativeFlag(true);
    myTestCPU.setOverflowFlag(true);
    writeword(0xB00, 0x102C);
    writeword(0xB02, 0x03FF);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(5);
    assertEquals(0xB00 + 4 + 0x03FF, myTestCPU.getProgramCounter());
  }

  @Test
  public void testJMPExtended() {
    myTestCPU.write(0xB00, 0x7E); // JMP EXTENDED
    writeword(0xB01, 0x102C);
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(4);
    assertEquals(0x102C, myTestCPU.getProgramCounter());
  }

  @Test
  public void testJMPIndexed() {
    myTestCPU.write(0xB00, 0x6E); // JMP INDEXED
    writebyte(0xB01, 0x41);       // 1,U
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.setUserStackPointer(0xE015);
    myTestCPU.emulateCycles(5);
    // Due to the way the JMP INDEXED instruction is implemented, the PC is not set until
    // immediately before the next opcode fetch. So PC is expected to be one more than what
    // the JMP set it to, since the next opcode was already fetched.
    assertEquals(0xE017, myTestCPU.getProgramCounter());
  }
  
  @Test
  public void testRTS() {
    myTestCPU.setStackPointer(0x300);
    writeword(0x300, 0x102C); // Write return address
    myTestCPU.write(0xB00, 0x39); // RTS
    myTestCPU.setProgramCounter(0xB00);
    myTestCPU.emulateCycles(5);
    assertEquals(0x102C, myTestCPU.getProgramCounter());
    assertEquals(0x302, myTestCPU.getStackPointer());
  }
}
