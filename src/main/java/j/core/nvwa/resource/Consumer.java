package j.core.nvwa.resource;

import j.core.annotation.description.ClassDescription;
import j.core.annotation.description.MethodDescription;

@ClassDescription(author = "肖炯",
        date = "2021/07/19",
        description = "资源扫描器首次扫描到资源（class、xml、properties等）、或扫描到资源变更时，会将类交给配置Consumer（可配置多个）去处理。",
        reviewers = {})
public interface Consumer {
    /**
     *
     * @param resource 需要处理的资源
     * @return
     */
    @MethodDescription(author = "肖炯",
            date = "2021/07/19",
            description = "首次扫描到资源时执行的操作")
    public boolean onFound(Resource resource);


    /**
     *
     * @param resource 需要处理的资源
     * @return
     */
    @MethodDescription(author = "肖炯",
            date = "2021/07/19",
            description = "资源文件变更时执行的操作")
    public boolean onUpdate(Resource resource);
}