package emu.jpoly.memory;

import com.badlogic.gdx.Gdx;

import emu.jpoly.cpu.Cpu6809SingleCycle;
import emu.jpoly.io.Acia6850;
import emu.jpoly.io.Via6522;

/**
 * This class emulators the JPoly's memory.
 * 
 * @author Lance Ewing
 */
public class Memory {

  /**
   * Holds an array of references to instances of MemoryMappedChip where each
   * instance determines the behaviour of reading or writing to the given memory
   * address.
   */
  private MemoryMappedChip memoryMap[];
  
  /**
   * The Cpu6809 that will be accessing this Memory.
   */
  private Cpu6809SingleCycle cpu;
  
  /**
   * Constructor for Memory. Mainly available for unit testing.
   * 
   * @param cpu The CPU that will access this Memory.
   * @param allRam true if memory should be initialised to all RAM; otherwise false.
   */
  public Memory(Cpu6809SingleCycle cpu, boolean allRam) {
    this.memoryMap = new MemoryMappedChip[65536];
    this.cpu = cpu;
    cpu.setMemory(this);
    if (allRam) {
      mapChipToMemory(new RamChip(0x10000), 0x0000, 0xFFFF);
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
   * Initialises the memory in such a way as to emulate Grant Searle's Simple6809 computer. An
   * amazingly concise 6809 computer that I think deserves to be a test architecture for any 
   * new 6809 emulation. Check out Grant's Simple6809 web page here:
   * 
   * http://searle.hostei.com/grant/6809/Simple6809.html
   */
  public void initGrantSearleSimple6809Memory(Acia6850 acia) {
    //    0000-7FFF 32K RAM (A15 == 0)
    mapChipToMemory(new RamChip(0x8000), 0x0000, 0x7FFF);
    
    //    8000-9FFF FREE SPACE (8K)
    mapChipToMemory(new UnconnectedMemory(), 0x8000, 0x9FFF);
    
    //    A000-BFFF SERIAL INTERFACE (minimally decoded) (A15 == 1, A14 == 0, A13 == 1) 101X XXXX XXXX XXX0/1
    mapChipToMemory(acia, 0xA000, 0xBFFF);
    
    //    C000-FFFF 16K ROM (BASIC from DB00 TO FFFF, so a large amount of free space suitable for a monitor etc) (A14 == 1 && A15 == 1)
    mapChipToMemory(new RomChip(convertByteArrayToIntArray(Gdx.files.internal("roms/ExBasROM.bin").readBytes())), 0xC000, 0xFFFF);
  }
  
  /**
   * Initialises the memory map for Vectrex emulation.
   */
  public void initVectrexMemory(Via6522 via) {
    // 0000-7fff Cartridge ROM Space. Without a cartridge, it is unconnected.
    mapChipToMemory(new UnconnectedMemory(), 0x0000, 0x7FFF);

    // 8000-C7FF Unmapped space.
    mapChipToMemory(new UnconnectedMemory(), 0x8000, 0xC7FF);

    // C800-CFFF Vectrex RAM Space 1Kx8, shadowed twice. (r/w)
    RamChip ram = new RamChip(0x0400);
    mapChipToMemory(ram, 0xC800, 0xCFFF);
    
    // D000-D7FF 6522 VIA shadowed 128 times (r/w)
    mapChipToMemory(via, 0xD000, 0xD7FF);

    // D800-DFFF Don't use this area. Both the 6522 and RAM are selected in any reads/writes to this area.
    NotFullyDecodedMemory ramAndVia = new NotFullyDecodedMemory(new MemoryMappedChip[] { ram, via});
    mapChipToMemory(ramAndVia, 0xD800, 0xDFFF);

    // [A15 == 1 && A14 == 1 && A13 == 1] 
    // E000-FFFF System ROM Space 8Kx8 (r/w)
    // E000-EFFF is ROM, the built in game MINE STORM.
    // F000-FFFF Executive (power-up / reset handler and a large selection of subroutines for drawing, calculation, game logic and / or hardware maintenance)
    mapChipToMemory(new RomChip(convertByteArrayToIntArray(Gdx.files.internal("roms/vectrex_rom.bin").readBytes())), 0xE000, 0xFFFF);
  }
  
  /**
   * Initialise the Poly's memory.
   */
  public void initPolyMemory() {

    
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
   * Maps the given chip instance at the given add.lengthress range.
   * 
   * @param chip The chip to map at the given address range.
   * @param startAddress The start of the address range.
   * @param endAddress The end of the address range.
   */
  public void mapChipToMemory(MemoryMappedChip chip, int startAddress, int endAddress) {
    // Configure the chip into the memory map between the given start and end addresses.
    for (int i = startAddress; i <= endAddress; i++) {
      memoryMap[i] = chip;
    }

    chip.setMemory(this);
  }
  
  /**
   * Loads a ROM file from the given byte array at the given memory address.
   * 
   * @param romData The byte array containing the ROM program data to load.
   */
  public void loadCustomRom(int address, byte[] romData) {
    mapChipToMemory(new RomChip(convertByteArrayToIntArray(romData)), address, address + (romData.length - 1));
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
