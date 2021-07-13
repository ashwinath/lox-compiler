#ifndef clox_memory_h
#define clox_memory_h

#include "common.h"

#define GROW_CAPACITY(capacity) \
    ((capacity) < 8 ? 8 : (capacity) * 2)

#define GROW_ARRAY(type, pointer, oldCount, newCount) \
    (type*)reallocate(pointer, sizeof(type) * (oldCount), sizeof(type) * (newCount))

#define FREE_ARRAY(type, pointer, oldCount) \
    reallocate(pointer, sizeof(type) * (oldCount), 0)

/*
 * reallocate will do the following:
 * 1. Allocate a new block
 * 2. Free allocation
 * 3. Shrink allocation
 * 4. Grow allocation
 */
void* reallocate(void* pointer, size_t oldSize, size_t newSize);

#endif
