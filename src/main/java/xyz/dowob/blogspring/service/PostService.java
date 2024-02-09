package xyz.dowob.blogspring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import xyz.dowob.blogspring.Exceptions.Postdata_UpdateException;
import xyz.dowob.blogspring.model.Post;
import xyz.dowob.blogspring.model.User;
import xyz.dowob.blogspring.repository.PostRepository;

import java.util.List;

@Service
public class PostService {
    private final PostRepository postRepository;


    @Autowired
    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;

    }

    public void addNewPost(Post post, User author) throws Postdata_UpdateException {
        if (post.getTitle().isEmpty() || post.getContent().isEmpty()) throw new Postdata_UpdateException(Postdata_UpdateException.ErrorCode.POSTDATA_UPDATE_FAILED);
        if (post.getTitle().length() > 250) throw new Postdata_UpdateException(Postdata_UpdateException.ErrorCode.TITLE_TOO_LONG);
        if (post.getContent().length() > 21000) throw new Postdata_UpdateException(Postdata_UpdateException.ErrorCode.CONTENT_TOO_LONG);
        post.setAuthor(author);
        post.setTitle(post.getTitle());
        post.setContent(post.getContent());
        postRepository.save(post);

    }
    //找到所有文章
    public List<Post> findAllPosts(){
        return postRepository.findAll();
    }

    public Page<Post> allPostsByPage(Pageable pageable){
        return postRepository.findAll(pageable);
    }

    public Post findPostByArticle_id(Long articleId) throws Postdata_UpdateException {

        return postRepository.findByArticleId(articleId)
                .orElseThrow(() -> new Postdata_UpdateException(Postdata_UpdateException.ErrorCode.POST_NOT_FOUND));
    }

}
