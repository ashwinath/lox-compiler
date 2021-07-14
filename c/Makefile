BUILD_FLAGS = -Wall -Werror -Wno-unused-parameter -Wextra

build:
	@mkdir -p bin
	@gcc $(BUILD_FLAGS) \
		-std=c99 \
		-o bin/clox \
		src/*.c

debug:
	@mkdir -p bin
	@gcc $(BUILD_FLAGS) \
		-g \
		-std=c99 \
		-o bin/clox \
		src/*.c
	@gdb bin/clox
