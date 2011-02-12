package capanalyzer.netutils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class MyBufferedInputStream extends FilterInputStream
{

	private static int defaultBufferSize = 8192;

	/**
	 * The internal buffer array where the data is stored. When necessary, it
	 * may be replaced by another array of a different size.
	 */
	protected volatile byte buf[];

	/**
	 * Atomic updater to provide compareAndSet for buf. This is necessary
	 * because closes can be asynchronous. We use nullness of buf[] as primary
	 * indicator that this stream is closed. (The "in" field is also nulled out
	 * on close.)
	 */
	private static final AtomicReferenceFieldUpdater<MyBufferedInputStream, byte[]> bufUpdater = 
		AtomicReferenceFieldUpdater.newUpdater(MyBufferedInputStream.class, byte[].class, "buf");

	/**
	 * The index one greater than the index of the last valid byte in the
	 * buffer. This value is always in the range <code>0</code> through
	 * <code>buf.length</code>; elements <code>buf[0]</code> through
	 * <code>buf[count-1]
	 * </code>contain buffered input data obtained from the underlying input
	 * stream.
	 */
	protected int count;

	/**
	 * The current position in the buffer. This is the index of the next
	 * character to be read from the <code>buf</code> array.
	 * <p>
	 * This value is always in the range <code>0</code> through
	 * <code>count</code>. If it is less than <code>count</code>, then
	 * <code>buf[pos]</code> is the next byte to be supplied as input; if it is
	 * equal to <code>count</code>, then the next <code>read</code> or
	 * <code>skip</code> operation will require more bytes to be read from the
	 * contained input stream.
	 * 
	 * @see java.io.BufferedInputStream#buf
	 */
	protected int pos;

	
	/**
	 * Check to make sure that underlying input stream has not been nulled out
	 * due to close; if not return it;
	 */
	private InputStream getInIfOpen() throws IOException
	{
		InputStream input = in;
		if (input == null)
			throw new IOException("Stream closed");
		return input;
	}

	/**
	 * Check to make sure that buffer has not been nulled out due to close; if
	 * not return it;
	 */
	private byte[] getBufIfOpen() throws IOException
	{
		byte[] buffer = buf;
		if (buffer == null)
			throw new IOException("Stream closed");
		return buffer;
	}

	/**
	 * Creates a <code>MyBufferedInputStream</code> and saves its argument, the
	 * input stream <code>in</code>, for later use. An internal buffer array is
	 * created and stored in <code>buf</code>.
	 * 
	 * @param in
	 *            the underlying input stream.
	 */
	public MyBufferedInputStream(InputStream in)
	{
		this(in, defaultBufferSize);
	}

	/**
	 * Creates a <code>MyBufferedInputStream</code> with the specified buffer
	 * size, and saves its argument, the input stream <code>in</code>, for later
	 * use. An internal buffer array of length <code>size</code> is created and
	 * stored in <code>buf</code>.
	 * 
	 * @param in
	 *            the underlying input stream.
	 * @param size
	 *            the buffer size.
	 * @exception IllegalArgumentException
	 *                if size <= 0.
	 */
	public MyBufferedInputStream(InputStream in, int size)
	{
		super(in);
		if (size <= 0)
		{
			throw new IllegalArgumentException("Buffer size <= 0");
		}
		buf = new byte[size];
	}

	/**
	 * Fills the buffer with more data. Assumes that it is being called by a
	 * synchronized method. This method also assumes that all data has already
	 * been read in, hence pos > count.
	 */
	private void fill() throws IOException
	{
		byte[] buffer = getBufIfOpen();
		pos = 0; /* throw away the buffer */
		
		count = pos;
		int n = getInIfOpen().read(buffer, pos, buffer.length - pos);
		if (n > 0)
			count = n + pos;
	}

	/**
	 * See the general contract of the <code>read</code> method of
	 * <code>InputStream</code>.
	 * 
	 * @return the next byte of data, or <code>-1</code> if the end of the
	 *         stream is reached.
	 * @exception IOException
	 *                if this input stream has been closed by invoking its
	 *                {@link #close()} method, or an I/O error occurs.
	 * @see java.io.FilterInputStream#in
	 */
	public synchronized int read() throws IOException
	{
		if (pos >= count)
		{
			fill();
			if (pos >= count)
				return -1;
		}
		return getBufIfOpen()[pos++] & 0xff;
	}

	/**
	 * Read characters into a portion of an array, reading from the underlying
	 * stream at most once if necessary.
	 */
	private int read1(byte[] b, int off, int len) throws IOException
	{
		int avail = count - pos;
		if (avail <= 0)
		{
			/*
			 * If the requested length is at least as large as the buffer, and
			 * if there is no mark/reset activity, do not bother to copy the
			 * bytes into the local buffer. In this way buffered streams will
			 * cascade harmlessly.
			 */
			if (len >= getBufIfOpen().length)
			{
				return getInIfOpen().read(b, off, len);
			}
			fill();
			avail = count - pos;
			if (avail <= 0)
				return -1;
		}
		int cnt = (avail < len) ? avail : len;
		System.arraycopy(getBufIfOpen(), pos, b, off, cnt);
		pos += cnt;
		return cnt;
	}

	/**
	 * Reads bytes from this byte-input stream into the specified byte array,
	 * starting at the given offset.
	 * 
	 * <p>
	 * This method implements the general contract of the corresponding
	 * <code>{@link InputStream#read(byte[], int, int) read}</code> method of
	 * the <code>{@link InputStream}</code> class. As an additional convenience,
	 * it attempts to read as many bytes as possible by repeatedly invoking the
	 * <code>read</code> method of the underlying stream. This iterated
	 * <code>read</code> continues until one of the following conditions becomes
	 * true:
	 * <ul>
	 * 
	 * <li>The specified number of bytes have been read,
	 * 
	 * <li>The <code>read</code> method of the underlying stream returns
	 * <code>-1</code>, indicating end-of-file, or
	 * 
	 * <li>The <code>available</code> method of the underlying stream returns
	 * zero, indicating that further input requests would block.
	 * 
	 * </ul>
	 * If the first <code>read</code> on the underlying stream returns
	 * <code>-1</code> to indicate end-of-file then this method returns
	 * <code>-1</code>. Otherwise this method returns the number of bytes
	 * actually read.
	 * 
	 * <p>
	 * Subclasses of this class are encouraged, but not required, to attempt to
	 * read as many bytes as possible in the same fashion.
	 * 
	 * @param b
	 *            destination buffer.
	 * @param off
	 *            offset at which to start storing bytes.
	 * @param len
	 *            maximum number of bytes to read.
	 * @return the number of bytes read, or <code>-1</code> if the end of the
	 *         stream has been reached.
	 * @exception IOException
	 *                if this input stream has been closed by invoking its
	 *                {@link #close()} method, or an I/O error occurs.
	 */
	public synchronized int read(byte b[], int off, int len) throws IOException
	{
		getBufIfOpen(); // Check for closed stream
		if ((off | len | (off + len) | (b.length - (off + len))) < 0)
		{
			throw new IndexOutOfBoundsException();
		} else if (len == 0)
		{
			return 0;
		}

		int n = 0;
		for (;;)
		{
			int nread = read1(b, off + n, len - n);
			if (nread <= 0)
				return (n == 0) ? nread : n;
			n += nread;
			if (n >= len)
				return n;
			// if not closed but no bytes available, return
		//LL	InputStream input = in;
		//LL	if (input != null && input.available() <= 0)
		//LL		return n;
		}
	}

	/**
	 * See the general contract of the <code>skip</code> method of
	 * <code>InputStream</code>.
	 * 
	 * @exception IOException
	 *                if the stream does not support seek, or if this input
	 *                stream has been closed by invoking its {@link #close()}
	 *                method, or an I/O error occurs.
	 */
	public synchronized long skip(long n) throws IOException
	{
		getBufIfOpen(); // Check for closed stream
		if (n <= 0)
		{
			return 0;
		}
		long avail = count - pos;

		if (avail <= 0)
		{
			return getInIfOpen().skip(n);
		}

		//LL long skipped = (avail < n) ? avail : n;
		//LL pos += skipped;
		//LL return skipped;
		
		//LL added the rest form here
		long skipped = 0;
		long remainingToSkip = n;
		if(avail < n)
		{
			skipped = avail;
			while(avail < remainingToSkip)
			{
				fill();
				avail = count - pos;
				skipped += avail;
				remainingToSkip -= avail;
			}
			skipped+=remainingToSkip;
			pos += remainingToSkip;
		}
		else
		{
			skipped = n;
			pos += skipped;
		}
		
		return skipped;	
	}

	/**
	 * Returns an estimate of the number of bytes that can be read (or skipped
	 * over) from this input stream without blocking by the next invocation of a
	 * method for this input stream. The next invocation might be the same
	 * thread or another thread. A single read or skip of this many bytes will
	 * not block, but may read or skip fewer bytes.
	 * <p>
	 * This method returns the sum of the number of bytes remaining to be read
	 * in the buffer (<code>count&nbsp;- pos</code>) and the result of calling
	 * the {@link java.io.FilterInputStream#in in}.available().
	 * 
	 * @return an estimate of the number of bytes that can be read (or skipped
	 *         over) from this input stream without blocking.
	 * @exception IOException
	 *                if this input stream has been closed by invoking its
	 *                {@link #close()} method, or an I/O error occurs.
	 */
	public synchronized int available() throws IOException
	{
		return getInIfOpen().available() + (count - pos);
	}


	/**
	 * Closes this input stream and releases any system resources associated
	 * with the stream. Once the stream has been closed, further read(),
	 * available(), reset(), or skip() invocations will throw an IOException.
	 * Closing a previously closed stream has no effect.
	 * 
	 * @exception IOException
	 *                if an I/O error occurs.
	 */
	public void close() throws IOException
	{
		byte[] buffer;
		while ((buffer = buf) != null)
		{
			if (bufUpdater.compareAndSet(this, buffer, null))
			{
				InputStream input = in;
				in = null;
				if (input != null)
					input.close();
				return;
			}
			// Else retry in case a new buf was CASed in fill()
		}
	}
}
