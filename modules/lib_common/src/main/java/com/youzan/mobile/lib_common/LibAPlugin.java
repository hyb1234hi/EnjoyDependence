package com.youzan.mobile.lib_common;

/**
 * 类似于mediator的方案，在第三方定义API
 */
public abstract class LibAPlugin implements IPlugin{
    public abstract void say();

    public abstract void ask();
}
