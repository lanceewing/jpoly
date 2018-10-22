package emu.jpoly.memory;

import emu.jpoly.cpu.Cpu6809SingleCycle;

/**
 * This class emulators the JPoly's memory.
 * 
 * @author Lance Ewing
 */
public class Memory {
  
  /**
   * Holds the machines memory.
   */
  private int mem[];

  /**
   * Holds an array of references to instances of MemoryMappedChip where each
   * instance determines the behaviour of reading or writing to the given memory
   * address.
   */
  private MemoryMappedChip memoryMap[];
  
  /**
   * The type of BASIC ROM being used.
   */
  private RomType romType;
  
  /**
   * The Cpu6809 that will be accessing this Memory.
   */
  private Cpu6809SingleCycle cpu;
  
  /**
   * Whether the BASIC ROM is disabled or not.
   */
  private boolean basicRomDisabled;
  
  /**
   * Whether the Microdisc ROM is enabled or not.
   */
  private boolean diskRomEnabled;
  
  /**
   * The 16 KB content of the loaded BASIC ROM.
   */
  private int[] basicRom;
  
  /**
   * The 8 KB content of the loaded Microdisk ROM.
   */
  private int[] microdiscRom;
  
  /**
   * Constructor for Memory. Mainly available for unit testing.
   * 
   * @param cpu The CPU that will access this Memory.
   * @param allRam true if memory should be initialised to all RAM; otherwise false.
   */
  public Memory(Cpu6809SingleCycle cpu, boolean allRam) {
    this.mem = new int[65536];
    this.memoryMap = new MemoryMappedChip[65536];
    this.cpu = cpu;
    cpu.setMemory(this);
    if (allRam) {
      mapChipToMemory(new RamChip(), 0x0000, 0xFFFF);
    }
  }
  
  /**
   * Constructor for Memory.
   * 
   * @param cpu The CPU that will access this Memory.
   */
  public Memory(Cpu6809SingleCycle cpu) {
    this(cpu, false);
    initPolyMemory();
  }

  /**
   * Initialise the Poly's memory.
   */
  private void initPolyMemory() {
    // The initial RAM pattern cannot simply be all zeroes.
    for (int i = 0; i <= 0xFFFF; ++i) {
      this.mem[i] = ((i & 128) != 0 ? 0xFF : 0);
    }
    

  }
  
  /**
   * Converts a byte array into an int array.
   * 
   * @param data The byte array to convert.
   * 
   * @return The int array.
   */
  private int[] convertByteArrayToIntArray(byte[] data) {
    int[] convertedData = new int[data.length];
    for (int i=0; i<data.length; i++) {
      convertedData[i] = ((int)data[i]) & 0xFF;
    }
    return convertedData;
  }
  
  /**
   * Maps the given chip instance at the given address range.
   * 
   * @param chip The chip to map at the given address range.
   * @param startAddress The start of the address range.
   * @param endAddress The end of the address range.
   */
  private void mapChipToMemory(MemoryMappedChip chip, int startAddress, int endAddress) {
    mapChipToMemory(chip, startAddress, endAddress, null);
  }
  
  /**
   * Maps the given chip instance at the given address range, optionally loading the
   * given initial state data into that address range. This state data is intended to be
   * used for things such as ROM images (e.g. char, kernel, basic).
   * 
   * @param chip The chip to map at the given address range.
   * @param startAddress The start of the address range.
   * @param endAddress The end of the address range.
   * @param state byte array containing initial state (can be null).
   */
  private void mapChipToMemory(MemoryMappedChip chip, int startAddress, int endAddress, byte[] state) {
    int statePos = 0;
    
    // Load the initial state into memory if provided.
    if (state != null) {
      for (int i=startAddress; i<=endAddress; i++) {
        mem[i] = (state[statePos++] & 0xFF);
      }
    }
    
    // Configure the chip into the memory map between the given start and end addresses.
    for (int i = startAddress; i <= endAddress; i++) {
      memoryMap[i] = chip;
    }

    chip.setMemory(this);
  }
  
  /**
   * Gets the BASIC ROM type being used.
   * 
   * @return The BASIC ROM type being used.
   */
  public RomType getRomType() {
    return romType;
  }
  
  /**
   * Enum representing the different BASIC ROM types supported by JOric. It holds
   * the details that are specific to a particular ROM type, such as start addresses
   * of various ROM routines or RAM addresses used by those ROM routines.
   */
  public static enum RomType {
    
    ATMOS(0xC592, 0xE6C9, 0xE735, 0x027F, 0x0293, 0x02A7, 0xE65E, 0xE75E, 0x024D, 0xE6FB), 
    ORIC1(0xC5A2, 0xE630, 0xE696, 0x0035, 0x0049, 0x005D, 0xE5C6, 0xE6BE, 0x0067, 0xE65D),  
    CUSTOM(0, 0, 0, 0, 0, 0, 0, 0, 0, 0),  
    DISABLED(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    
    private int addressOfInputLineFromKeyboard;   // Used to automatically run small snippets of BASIC
    private int addressOfReadByteFromTape;
    private int addressOfGetInSyncWithTapeData;
    private int addressOfFileToLoadFromTape; 
    private int addressOfFileLoadedFromTape;
    private int addressOfTapeHeader;
    private int addressOfOutputByteToTape;
    private int addressOfOutputTapeLeader;
    private int addressOfTapeSpeed;                // 0 = fast, >= 1 = slow
    private int addressOfRTS;                      // Used by subroutine traps as a miscellaneous RTS address to position PC at after running trap.
    
    /**
     * Constructor for RomType.
     * 
     * @param addressOfInputLineFromKeyboard
     * @param addressOfReadByteFromTape
     * @param addressOfGetInSyncWithTapeData
     * @param addressOfFileToLoadFromTape
     * @param addressOfFileLoadedFromTape
     * @param addressOfTapeHeader
     * @param addressOfOutputByteToTape
     * @param addressOfOutputTapeLeader
     * @param addressOfTapeSpeed
     * @param addressOfRTS
     */
    RomType(int addressOfInputLineFromKeyboard, int addressOfReadByteFromTape, 
            int addressOfGetInSyncWithTapeData, int addressOfFileToLoadFromTape, 
            int addressOfFileLoadedFromTape, int addressOfTapeHeader,
            int addressOfOutputByteToTape, int addressOfOutputTapeLeader,
            int addressOfTapeSpeed, int addressOfRTS) {
      
      this.addressOfInputLineFromKeyboard = addressOfInputLineFromKeyboard;
      this.addressOfReadByteFromTape = addressOfReadByteFromTape;
      this.addressOfGetInSyncWithTapeData = addressOfGetInSyncWithTapeData;
      this.addressOfFileToLoadFromTape = addressOfFileToLoadFromTape;
      this.addressOfFileLoadedFromTape = addressOfFileLoadedFromTape;
      this.addressOfTapeHeader = addressOfTapeHeader;
      this.addressOfOutputByteToTape = addressOfOutputByteToTape;
      this.addressOfOutputTapeLeader = addressOfOutputTapeLeader;
      this.addressOfTapeSpeed = addressOfTapeSpeed;
      this.addressOfRTS = addressOfRTS;
    }

    public int getAddressOfInputLineFromKeyboard() {
      return addressOfInputLineFromKeyboard;
    }

    public int getAddressOfReadByteFromTape() {
      return addressOfReadByteFromTape;
    }

    public int getAddressOfGetInSyncWithTapeData() {
      return addressOfGetInSyncWithTapeData;
    }

    public int getAddressOfFileToLoadFromTape() {
      return addressOfFileToLoadFromTape;
    }

    public int getAddressOfFileLoadedFromTape() {
      return addressOfFileLoadedFromTape;
    }

    public int getAddressOfTapeHeader() {
      return addressOfTapeHeader;
    }

    public int getAddressOfOutputByteToTape() {
      return addressOfOutputByteToTape;
    }

    public int getAddressOfOutputTapeLeader() {
      return addressOfOutputTapeLeader;
    }

    public int getAddressOfTapeSpeed() {
      return addressOfTapeSpeed;
    }

    public int getAddressOfRTS() {
      return addressOfRTS;
    }
  }
  
  /**
   * Loads a ROM file from the given byte array in to the BASIC ROM area.
   * 
   * @param romData The byte array containing the ROM program data to load.
   */
  public void loadCustomRom(byte[] romData) {
    mapChipToMemory(new RomChip(), 0xC000, 0xC000 + (romData.length - 1), romData);
  }
  
  /**
   * Gets the int array that represents the Oric's memory.
   * 
   * @return an int array represents the Oric memory.
   */
  public int[] getMemoryArray() {
    return mem;
  }

  /**
   * Gets the array of memory mapped devices. 
   * 
   * @return The array of memory mapped devices.
   */
  public MemoryMappedChip[] getMemoryMap() {
    return memoryMap;
  }
  
  /**
   * Forces a write to a memory address, even if it is ROM. This is used mainly
   * for setting emulation traps.
   * 
   * @param address The address to write the value to.
   * @param value The value to write to the given address.
   */
  public void forceWrite(int address, int value) {
    if (address < 0xC000) {
      writeMemory(address, value);
    } else {
      if (basicRomDisabled) {
        if (diskRomEnabled) {
          microdiscRom[address - 0xE000] = value;
        } else {
          mem[address] = value;
        }
      } else {
        basicRom[address - 0xC000] = value;
      }
    }
  }
  
  /**
   * Reads the value of the given Oric memory address.
   * 
   * @param address The address to read the byte from.
   * 
   * @return The contents of the memory address.
   */
  public int readMemory(int address) {
    return (memoryMap[address].readMemory(address));
  }

  /**
   * Writes a value to the give Oric memory address.
   * 
   * @param address The address to write the value to.
   * @param value The value to write to the given address.
   */
  public void writeMemory(int address, int value) {
    memoryMap[address].writeMemory(address, value);
  }
}
