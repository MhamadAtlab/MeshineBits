/**
 * 
 */
package meshIneBits.config.patternParameter;

/**
 * @author Quoc Nhat Han TRAN
 *
 */
public class BooleanParam extends PatternParameter {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2259784876889236537L;
	private boolean currentValue;
	private boolean defaultValue;

	/**
	 * @param name
	 *            Should be unique among parameters of a pattern
	 * @param title
	 * @param description
	 * @param defaultValue
	 *            which value this parameter should hold at first or when meet a
	 *            wrong setting
	 * @param currentValue
	 *            current state of parameter
	 */
	public BooleanParam(String name, String title, String description, boolean defaultValue, boolean currentValue) {
		this.title = title;
		this.codename = name;
		this.description = description;
		this.defaultValue = defaultValue;
		this.currentValue = currentValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see meshIneBits.config.patternParameter.PatternParameter#getCurrentValue()
	 */
	@Override
	public Boolean getCurrentValue() {
		return currentValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * meshIneBits.config.patternParameter.PatternParameter#setCurrentValue(java.
	 * lang.Object)
	 */
	@Override
	public void setCurrentValue(Object newValue) {
		if (newValue instanceof Boolean)
			this.currentValue = (boolean) newValue;
		else
			this.currentValue = defaultValue;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}
}
