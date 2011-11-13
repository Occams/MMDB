package model;

public class NoImageLoadedException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public NoImageLoadedException()
	  {
			
	  }
	
	public NoImageLoadedException(String s)
	{
		super(s);	
	}
}
