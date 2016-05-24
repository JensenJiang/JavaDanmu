CC = gcc
CXX = g++
INCLUDE = -I/usr/lib/jvm/default/include -I/usr/lib/jvm/default/include/linux
CPPFLAGS = ${INCLUDE}
LDFLAGS = -shared -fPIC
LIBS = -lX11 -lXext

.PHONY: all clean

all: libdanmaQ_Window.so

libdanmaQ_Window.so: danmaQ_Window.cc danmaQ_Window.h
	${CXX} danmaQ_Window.cc ${CPPFLAGS} ${LDFLAGS} ${LIBS} -o libdanmaQ_Window.so

clean:
	-rm danmaQ_Window.so
