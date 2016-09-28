package org.fastutil.general.mail;

import org.fastutil.general.GeneralHelper;

import javax.activation.DataSource;
import java.io.*;


/** Byte Array 数据源，发送字节数组附件时用到 */
class ByteArrayDataSource implements DataSource
{
	public static final String DEFAULT_ENCODING	= GeneralHelper.DEFAULT_ENCODING;
	
	private ByteArrayOutputStream	baos		= null;
	private String					type		= "application/octet-stream";
	private String					name		= "ByteArrayDataSource";
	
	private void init(String type, String name)
	{
		if(type != null)
			this.type = type;
		if(name != null)
			this.name = name;		
	}

	public ByteArrayDataSource(byte[] data, String type, String name) throws IOException
	{
		ByteArrayInputStream Bis = null;

		try
		{
			Bis = new ByteArrayInputStream(data);
			this.byteArrayDataSource(Bis, type, name);
		}
		catch (IOException ioex)
		{
			throw ioex;
		}
		finally
		{
			try
			{
				if (Bis != null)
				{
					Bis.close();
				}
			}
			catch (IOException ignored)
			{
			}
		}

	}

	public ByteArrayDataSource(InputStream aIs, String type, String name) throws IOException
	{
		this.byteArrayDataSource(aIs, type, name);
	}

	private void byteArrayDataSource(InputStream aIs, String type, String name) throws IOException
	{
		init(type, name);
		
		BufferedInputStream bis		= null;
		BufferedOutputStream bos	= null;
		
		try
		{
			int length = 0;
			byte[] buffer = new byte[4096];

			bis		= new BufferedInputStream(aIs);
			baos	= new ByteArrayOutputStream();
			bos		= new BufferedOutputStream(baos);

			while ((length = bis.read(buffer)) != -1)
			{
				bos.write(buffer, 0, length);
			}		
		}
		catch (IOException ioex)
		{
			throw ioex;
		}
		finally
		{
			try
			{
				if (bis != null)
				{
					bis.close();
				}
				if (baos != null)
				{
					baos.close();
				}
				if (bos != null)
				{
					bos.close();
				}
			}
			catch (IOException ignored)
			{
			}
		}
	}

	public ByteArrayDataSource(String data, String type, String name) throws IOException
	{
		this(data, DEFAULT_ENCODING, type, name);
	}

	public ByteArrayDataSource(String data, String encoding, String type, String name) throws IOException
	{
		init(type, name);

		try
		{
			baos = new ByteArrayOutputStream();

			baos.write(data.getBytes(encoding));
		}
		catch (UnsupportedEncodingException uex)
		{
			// Do something!
		}
		catch (IOException ignored)
		{
			// Ignore
		}
		finally
		{
			try
			{
				if (baos != null)
				{
					baos.close();
				}
			}
			catch (IOException ignored)
			{
			}
		}
	}

	@Override
	public String getContentType()
	{
		return type;
	}

	@Override
	public InputStream getInputStream() throws IOException
	{
		if (baos == null)
		{
			throw new IOException("no data");
		}
		
		return new ByteArrayInputStream(baos.toByteArray());
	}

	@Override
	public String getName()
	{
		return name;
	}

	@Override
	public OutputStream getOutputStream() throws IOException
	{
		baos = new ByteArrayOutputStream();
		return baos;
	}
}

