#include <unistd.h>

#include "io.h"

/*
http://www.cplusplus.com/forum/general/200941/

A lock is not required; send() is a syscall, it is an atomic operation with no race conditions in the kernel.

Note that datagram sockets (UDP) are connection-less, so the the datagram packets may be delivered in a sequence
that is different from the sequence in which they were sent (even if all the send calls were from within a single thread).

For stream sockets (TCP) too, the send() function is atomic; but there is no concept of distinct messages or packets,
the data treated as a single stream of bytes. So even though send() is thread-safe, synchronisation is required to
ensure that the bytes from different send calls are merged into the byte stream in a predictable manner.

https://stackoverflow.com/questions/13021796/simultaneously-read-and-write-on-the-same-socket-in-c-or-c

I dont need to worry about multiple threads read and writing from the same socket as there will be a single dedicated
read and single dedicated write thread writing to the socket. In the above scenario, is any kind of locking required?

No.
*/

int read_all(int fd, void *buf, int len) {
    int c = 0, t;
    while (c < len) {
        t = read(fd, ((char *)buf) + c, len - c);
        if (t < 0)
            return -1;
        c += t;
    }
    return 0;
}

int write_all(int fd, void *buf, int len) {
    int c = 0, t;
    while (c < len) {
        t = write(fd, ((char *)buf) + c, len - c);
        if (t < 0)
            return -1;
        c += t;
    }
    return 0;
}
