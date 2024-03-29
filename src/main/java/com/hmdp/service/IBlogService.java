package com.hmdp.service;

import com.hmdp.dto.Result;
import com.hmdp.entity.Blog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IBlogService extends IService<Blog> {

    /**
     * 查询具体博客
     * @param id
     * @return
     */
    Result queryBlogById(Long id);

    /**
     * 查询列表博客返回给首页
     * @param current
     * @return
     */
    Result queryHotBlog(Integer current);

    /**
     * 该用户是否点赞过这个博文
     * @param id
     * @return
     */
    Result likeBlog(Long id);

    Result queryBlogByIds(Long id);

    Result saveBlog(Blog blog);

    Result queryBlogOfFollow(Long max, Integer offset);
}
