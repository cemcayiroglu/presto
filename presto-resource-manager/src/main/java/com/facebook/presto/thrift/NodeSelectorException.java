package com.facebook.presto.thrift;

import com.facebook.drift.annotations.ThriftConstructor;
import com.facebook.drift.annotations.ThriftField;
import com.facebook.drift.annotations.ThriftStruct;

@ThriftStruct
public final class NodeSelectorException
        extends Exception
{
    private final boolean retryable;

    @ThriftConstructor
    public NodeSelectorException(String message, boolean retryable)
    {
        super(message);
        this.retryable = retryable;
    }

    @Override
    @ThriftField(1)
    public String getMessage()
    {
        return super.getMessage();
    }

    @ThriftField(2)
    public boolean isRetryable()
    {
        return retryable;
    }
}
