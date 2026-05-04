package j.core.nvwa;

import j.core.Startup;
import j.core.annotation.description.ClassDescription;
import j.core.annotation.description.MethodDescription;
import net.sf.cglib.proxy.*;

import java.lang.reflect.Method;

@ClassDescription(author = "肖炯",
		date = "2021/07/19",
		description = "通过cglib(动态代理)实现AOP编程",
		reviewers = {})
public class NvwaProxy<T> implements MethodInterceptor {
	private T realObject;

	/**
	 *
	 * @return
	 */
	public T getRealObject(){
		return this.realObject;
	}

	@Override
	@MethodDescription(author = "肖炯",
			date = "2021/07/19",
			description = "当通过bind()返回对象调用相关方法时，实际调用的是该方法，通过执行自定义的beforeInvoke、afterInvoke、onException可在执行实际方法前、后、异常时做个性化处理")
	public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
		try{
			this.beforeInvoke(method, objects);
			Object result = methodProxy.invokeSuper(o, objects);
			this.afterInvoke(method, objects, result);
			return result;
		}catch(Exception e){
			this.onException(method, objects, e);
			throw e;
		}
	}

	/**
	 *
	 * @param realObject
	 * @return
	 * @throws Exception
	 */
	@MethodDescription(author = "肖炯",
			date = "2021/07/19",
			description = "")
	public T bind(T realObject) throws Exception{
		this.realObject =realObject;

		Enhancer enhancer=new Enhancer();
		enhancer.setSuperclass(realObject.getClass());
		enhancer.setCallback(this);
		enhancer.setClassLoader(Startup.getDefaultClassLoader());

		return (T)enhancer.create();
	}

	/**
	 *
	 * @param realObject
	 * @param callbacks
	 * @param filter
	 * @return
	 * @throws Exception
	 */
	@MethodDescription(author = "肖炯",
			date = "2021/07/19",
			description = "")
	public T bind(T realObject, Callback[] callbacks, CallbackFilter filter) throws Exception{
		this.realObject = realObject;

		Enhancer enhancer=new Enhancer();
		enhancer.setSuperclass(realObject.getClass());
		enhancer.setCallbacks(callbacks);
		enhancer.setCallbackFilter(filter);
		enhancer.setClassLoader(Startup.getDefaultClassLoader());

		return (T)enhancer.create();
	}

	/**
	 *
	 * @param method 调用的方法
	 * @param args 调用该方法所需参数
	 * @return
	 * @throws Exception
	 */
	@MethodDescription(author = "肖炯",
			date = "2021/07/19",
			description = "调用实际方法前执行的操作")
	protected Object beforeInvoke(Method method, Object[] args) throws Exception{
		return null;
	}

	/**
	 *
	 * @param method 调用的方法
	 * @param args 调用该方法所需参数
	 * @param returnValue 调用该方法后返回的对象
	 * @return
	 * @throws Exception
	 */
	@MethodDescription(author = "肖炯",
			date = "2021/07/19",
			description = "调用实际方法后执行的操作")
	protected Object afterInvoke(Method method, Object[] args, Object returnValue) throws Exception{
		return null;
	}

	/**
	 *
	 * @param method 调用的方法
	 * @param args 调用该方法所需参数
	 * @param e 抛出的异常
	 * @return
	 * @throws Exception
	 */
	@MethodDescription(author = "肖炯",
			date = "2021/07/19",
			description = "调用实际方法出错时执行的操作")
	protected Object onException(Method method, Object[] args, Throwable e) throws Exception{
		return null;
	}
}