/*
 * Copyright (c) 2016 Seth J. Morabito <web@loomcom.com>
 *                    Maik Merten <maikmerten@googlemail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package emu.jpoly.io;

import emu.jpoly.cpu.Cpu6809SingleCycle;
import emu.jpoly.memory.MemoryMappedChip;

/**
 * This is a simulation of the Motorola 6850 ACIA, with limited functionality.
 * 
 * Unlike a 16550 UART, the 6850 ACIA has only one-byte transmit and receive
 * buffers. It is the programmer's responsibility to check the status (full or
 * empty) for transmit and receive buffers before writing / reading.
 */
public class Acia6850 extends MemoryMappedChip {

  private static final int STAT_REG = 0; // read-only
  private static final int CTRL_REG = 0; // write-only

  private static final int RX_REG = 1; // read-only
  private static final int TX_REG = 1; // write-only

  private Cpu6809SingleCycle cpu;

  private boolean receiveIrqEnabled = false;
  private boolean transmitIrqEnabled = false;
  private boolean overrun = false;
  private boolean interrupt = false;

  private long lastTxWrite = 0;
  private long lastRxRead = 0;
  private int baudRate = 0;
  private long baudRateDelay = 0;

  /**
   * Read/Write buffers
   */
  private int rxChar = 0;
  private int txChar = 0;

  private boolean rxFull = false;
  private boolean txEmpty = true;

  /**
   * Constructor for Acia6850.
   * 
   * @param cpu
   */
  public Acia6850(Cpu6809SingleCycle cpu) {
    this.cpu = cpu;
    setBaudRate(115200);   // From Simple6809
  }
  
  /*
   * Calculate the delay in nanoseconds between successive read/write
   * operations, based on the configured baud rate.
   */
  private long calculateBaudRateDelay() {
    if (baudRate > 0) {
      // TODO: This is a pretty rough approximation based on 8 bits per character, and 1/baudRate per bit. It could certainly be improved
      return (long) ((1.0 / baudRate) * 1000000000 * 8);
    } else {
      return 0;
    }
  }

  /**
   * Set the baud rate of the simulated ACIA.
   * 
   * @param rate The baud rate in bps. 0 means no simulated baud rate delay.
   */
  public void setBaudRate(int rate) {
    this.baudRate = rate;
    this.baudRateDelay = calculateBaudRateDelay();
  }

  public synchronized int rxRead(boolean cpuAccess) {
    if (cpuAccess) {
      lastRxRead = System.nanoTime();
      overrun = false;
      rxFull = false;
    }
    return rxChar;
  }

  public synchronized void rxWrite(int data) {
    if (rxFull) {
      overrun = true;
    }

    rxFull = true;

    if (receiveIrqEnabled) {
      interrupt = true;
      cpu.signalIRQ(true);
    }

    rxChar = data;
  }

  public synchronized int txRead(boolean cpuAccess) {
    if (cpuAccess) {
      txEmpty = true;

      if (transmitIrqEnabled) {
        interrupt = true;
        cpu.signalIRQ(true);
      }
    }
    return txChar;
  }

  public synchronized void txWrite(int data) {
    lastTxWrite = System.nanoTime();
    txChar = data;
    txEmpty = false;
  }

  /**
   * @return true if there is character data in the TX register.
   */
  public boolean hasTxChar() {
    return !txEmpty;
  }

  /**
   * @return true if there is character data in the RX register.
   */
  public boolean hasRxChar() {
    return rxFull;
  }

  private void setCommandRegister(int data) {
    // Bits 0 & 1 control the master reset
    if ((data & 0x01) != 0 && (data & 0x02) != 0) {
      reset();
    }

    // Bit 7 controls receiver IRQ behavior
    receiveIrqEnabled = (data & 0x80) != 0;
    // Bits 5 & 6 controls transmit IRQ behavior
    transmitIrqEnabled = (data & 0x20) != 0 && (data & 0x40) == 0;
  }

  /**
   * @return The contents of the status register.
   */
  public int statusReg(boolean cpuAccess) {
    // TODO: Parity Error, Framing Error, DTR, and DSR flags.
    int stat = 0;
    if (rxFull && System.nanoTime() >= (lastRxRead + baudRateDelay)) {
      stat |= 0x01;
    }
    if (txEmpty && System.nanoTime() >= (lastTxWrite + baudRateDelay)) {
      stat |= 0x02;
    }
    if (overrun) {
      stat |= 0x20;
    }
    if (interrupt) {
      stat |= 0x80;
    }

    if (cpuAccess) {
      interrupt = false;
    }

    return stat;
  }

  private synchronized void reset() {
    overrun = false;
    rxFull = false;
    txEmpty = true;
    interrupt = false;
  }

  @Override
  public int readMemory(int address) {
    switch (address & 0x0001) {
      case STAT_REG:
        return statusReg(true);
      case RX_REG:
        return rxRead(true);
    }
    return 0;
  }

  @Override
  public void writeMemory(int address, int data) {
    switch (address & 0x0001) {
      case CTRL_REG:
        setCommandRegister(data);
        break;
      case TX_REG:
        txWrite(data);
        break;
    }
  }
}