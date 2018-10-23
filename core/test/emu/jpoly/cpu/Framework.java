package emu.jpoly.cpu;

import static org.junit.Assert.assertEquals;

import org.junit.Before;

import emu.jpoly.cpu.Cpu6809SingleCycle;
import emu.jpoly.memory.Memory;

public class Framework {

    public interface GetSetInt {
      void set(int value);
      int intValue();
    }
    
    public interface RegisterCC {
      public void clear();
      public int intValue();
      public void setC(int i);
      public void set(int i);
      public void setV(int i);
      public void setN(int i);
      public int getC();
      public int getN();
      public int getZ();
      public int getV();
      public void setZ(int i);
      public int getI();
      public int getH();
      public boolean isSetE();
    }
  
    public interface Bus {
      public void signalIRQ(boolean state);
      public void signalFIRQ(boolean state);
      public void signalNMI(boolean state);
      public boolean isIRQActive();
      public boolean isNMIActive();
      public boolean isFIRQActive();
    }
    
    public class TestCPU extends Cpu6809SingleCycle {
      
      GetSetInt u = new GetSetInt() {
        public int intValue() {
          return getUserStackPointer();
        }
        public void set(int value) {
          setUserStackPointer(value);
        }
      };
      GetSetInt s = new GetSetInt() {
        public int intValue() {
          return getStackPointer();
        }
        public void set(int value) {
          setStackPointer(value);
        }
      };
      GetSetInt d = new GetSetInt() {
        public int intValue() {
          return getD();
        }
        public void set(int value) {
          setD(value);
        }
      };
      GetSetInt dp = new GetSetInt() {
        public int intValue() {
          return getDirectPageRegister();
        }
        public void set(int value) {
          setDirectPageRegister(value);
        }
      };
      GetSetInt pc = new GetSetInt() {
        public int intValue() {
          return getProgramCounter();
        }
        public void set(int value) {
          setProgramCounter(value);
        }
      };
      GetSetInt a = new GetSetInt() {
        public int intValue() {
          return getAccumulatorA();
        }
        public void set(int value) {
          setAccumulatorA(value);
        }
      };
      GetSetInt b = new GetSetInt() {
        public int intValue() {
          return getAccumulatorB();
        }
        public void set(int value) {
          setAccumulatorB(value);
        }
      };
      GetSetInt x = new GetSetInt() {
        public int intValue() {
          return getIndexRegisterX();
        }
        public void set(int value) {
          setIndexRegisterX(value);
        }
      };
      GetSetInt y = new GetSetInt() {
        public int intValue() {
          return getIndexRegisterY();
        }
        public void set(int value) {
          setIndexRegisterY(value);
        }
      };
      RegisterCC cc = new RegisterCC() {
        public void clear() {
          setCC(0);
        }
        public int intValue() {
          return getCC();
        }
        public void setC(int i) {
          setCarryFlag(i == 0? false : true);
        }
        public void set(int i) {
          setCC(i);
        }
        public void setV(int i) {
          setOverflowFlag(i == 0? false : true);
        }
        public void setN(int i) {
          setNegativeFlag(i == 0? false : true);
        }
        public int getC() {
          return (isCarryFlag()? 1 : 0);
        }
        public int getN() {
          return (isNegativeFlag()? 1 : 0);
        }
        public int getZ() {
          return (isZeroFlag()? 1 : 0);
        }
        public int getV() {
          return (isOverflowFlag()? 1 : 0);
        }
        public void setZ(int i) {
          setZeroFlag(i == 0? false : true);
        }
        public int getI() {
          return (isIrqDisableFlag()? 1 : 0);
        }
        public int getH() {
          return (isHalfCarryFlag()? 1 : 0);
        }
        public boolean isSetE() {
          return isEntireFlag();
        }
      };
            
      public int read(int address) {
        return fetch(address);
      }
      
      public Bus getBus() {
        return new Bus() {
          public void signalIRQ(boolean state) {
            TestCPU.this.signalIRQ(state);
          }
          public void signalFIRQ(boolean state) {
            TestCPU.this.signalFIRQ(state);
          }
          public void signalNMI(boolean state) {
            TestCPU.this.signalNMI(state);
          }
          public boolean isIRQActive() {
            return TestCPU.this.isIRQActive();
          }
          public boolean isNMIActive() {
            return TestCPU.this.isNMIActive();
          }
          public boolean isFIRQActive() {
            return TestCPU.this.isFIRQActive();
          }
        };
      }
    }

    public TestCPU myTestCPU;
    
    protected Memory memory;

    /**
     * Condition code names.
     */
    public enum CC {

        C, V, Z, N, I, H, F, E;

        public static final int Cmask = 1 << C.ordinal();
        public static final int Vmask = 1 << V.ordinal();
        public static final int Zmask = 1 << Z.ordinal();
        public static final int Nmask = 1 << N.ordinal();
        public static final int Imask = 1 << I.ordinal();
        public static final int Hmask = 1 << H.ordinal();
        public static final int Fmask = 1 << F.ordinal();
        public static final int Emask = 1 << E.ordinal();
    }
    
    @Before
    public void setUp() {
        myTestCPU = new TestCPU();
        memory = new Memory(myTestCPU, true);
    }

    void setA(int value) {
        myTestCPU.setAccumulatorA(value);
    }

    void assertA(int exp) {
        assertEquals(exp, myTestCPU.getAccumulatorA());
    }

    void setB(int value) {
        myTestCPU.setAccumulatorB(value);
    }

    void assertB(int exp) {
        assertEquals(exp, myTestCPU.getAccumulatorB());
    }

    void setD(int value) {
        myTestCPU.setD(value);;
    }

    void assertD(int exp) {
        assertEquals(exp, myTestCPU.getD());
    }

    void setX(int value) {
        myTestCPU.setIndexRegisterX(value);
    }

    void assertX(int exp) {
        assertEquals(exp, myTestCPU.getIndexRegisterX());
    }

    void setY(int value) {
        myTestCPU.setIndexRegisterY(value);
    }

    void assertY(int exp) {
        assertEquals(exp, myTestCPU.getIndexRegisterY());
    }

    void writebyte(int loc, int value) {
        memory.writeMemory(loc, value);
    }
    
    void writeword(int address, int value) {
        writebyte(address & 0xffff, (value >> 8) & 0xFF);
        address++;
        writebyte(address & 0xffff, value & 0xFF);
    }
    
    public int read(int address) {
      return memory.readMemory(address);
    }
    
    public int read_word(int address) {
      int tmp1 = read(address & 0xffff);
      address++;
      int tmp2 = read(address & 0xffff);
      return (tmp1 << 8) | tmp2;
    }
    
    protected static final int LOCATION = 0x1e20;
    
    /**
     * Load a short program into memory.
     */
    public void loadProg(int[] instructions) {
      writeword(0xfffe, LOCATION);
      int respc = read_word(0xfffe);
      assertEquals(LOCATION, respc);

      for (int i = 0; i < instructions.length; i++) {
        writebyte(i + LOCATION, instructions[i]);
      }

      myTestCPU.reset();           // Set's up for the hardware RESET instruction steps.
      myTestCPU.emulateCycles(7);  // Executes the hardware RESET instruction steps.
    }
    
    protected void chkCC_A_B_DP_X_Y_S_U(int cc, int a, int b, int dp, int x, int y, int s, int u) {
      assertEquals(cc, myTestCPU.getCC());
      assertA(a);
      assertEquals(b, myTestCPU.getAccumulatorB());
      assertEquals(dp, myTestCPU.getDirectPageRegister());
      assertEquals(x, myTestCPU.getIndexRegisterX());
      assertEquals(y, myTestCPU.getIndexRegisterY());
      assertEquals(s, myTestCPU.getStackPointer());
      assertEquals(u, myTestCPU.getUserStackPointer());
    }
    
    protected void setCC_A_B_DP_X_Y_S_U(int cc, int a, int b, int dp, int x, int y, int s, int u) {
      myTestCPU.setCC(cc);
      setA(a);
      setB(b);
      myTestCPU.setDirectPageRegister(dp);
      setX(x);
      setY(y);
      myTestCPU.setStackPointer(s);
      myTestCPU.setUserStackPointer(u);
    }
}
