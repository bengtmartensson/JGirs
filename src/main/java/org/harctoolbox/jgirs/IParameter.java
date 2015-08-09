package org.harctoolbox.jgirs;

/**
 * Defines the functions the Parameters must implement.
 *
 */
public interface IParameter {

    public String getName();

    public String getDocumentation();

    public void set(String value);

    public String get();
}
