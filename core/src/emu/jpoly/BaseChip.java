package emu.jpoly;

import emu.jpoly.memory.*;

/**
 * This is the base class of all chips.
 *
 * @author Lance Ewing
 */
public abstract class BaseChip {

  /**
   * Holds a reference to the machine's memory map.
   */
  protected Memory memory;

  /**
   * Holds an array of references to instances of MemoryMappedChip where each
   * instance determines the behaviour of reading or writing to the given memory
   * address.
   */
  protected MemoryMappedChip memoryMap[];
  
  /**
   * Sets a reference to the Oric memory map. 
   *  
   * @param memory The Oric memory map.
   */
  public void setMemory(Memory memory) {
    this.memory = memory;
    this.memoryMap = memory.getMemoryMap();
  }
}
