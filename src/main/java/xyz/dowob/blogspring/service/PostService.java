package xyz.dowob.blogspring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import xyz.dowob.blogspring.Exceptions.Postdata_UpdateException;
import xyz.dowob.blogspring.model.Post;
import xyz.dowob.blogspring.model.User;
import xyz.dowob.blogspring.repository.PostRepository;
import xyz.dowob.blogspring.repository.UserRepository;

import java.util.List;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;


    @Autowired
    public PostService(PostRepository postRepository, UserRepository userRepository){
        this.postRepository = postRepository;
        this.userRepository = userRepository;

    }

    public void addNewPost(Post post, String username) throws Postdata_UpdateException {
        if (username == null || username.trim().isEmpty()) throw new Postdata_UpdateException(Postdata_UpdateException.ErrorCode.DID_NOT_LOGIN);


        User author = userRepository.findByUsername(username).orElseThrow(() -> new Postdata_UpdateException(Postdata_UpdateException.ErrorCode.NOT_FOUND_USER));
        post.setAuthor(author);


        if (post.getTitle().isEmpty() || post.getContent().isEmpty()) throw new Postdata_UpdateException(Postdata_UpdateException.ErrorCode.POST_UPDATE_FAILED);
        if (post.getTitle().length() > 250) throw new Postdata_UpdateException(Postdata_UpdateException.ErrorCode.TITLE_TOO_LONG);
        post.setTitle(post.getTitle());

        if (post.getContent().length() > 21000) throw new Postdata_UpdateException(Postdata_UpdateException.ErrorCode.CONTENT_TOO_LONG);
        post.setContent(post.getContent());


        postRepository.save(post);

    }

    public void updatePost(Post UpdatePost, Post OriginPost) throws Postdata_UpdateException {
        if (UpdatePost.getTitle().isEmpty() || UpdatePost.getContent().isEmpty()) throw new Postdata_UpdateException(Postdata_UpdateException.ErrorCode.POST_UPDATE_FAILED);
        if (UpdatePost.getTitle().length() > 250) throw new Postdata_UpdateException(Postdata_UpdateException.ErrorCode.TITLE_TOO_LONG);
        OriginPost.setTitle(UpdatePost.getTitle());

        if (UpdatePost.getContent().length() > 21000) throw new Postdata_UpdateException(Postdata_UpdateException.ErrorCode.CONTENT_TOO_LONG);
        OriginPost.setContent(UpdatePost.getContent());

        postRepository.save(OriginPost);
    }



    //找到所有文章
    public List<Post> getAllPosts(){
        return postRepository.findAll();
    }



    public Post getPostByArticle_id(Long articleId) throws Postdata_UpdateException {

        return postRepository.findByArticleId(articleId)
                .orElseThrow(() -> new Postdata_UpdateException(Postdata_UpdateException.ErrorCode.POST_NOT_FOUND));
    }

    public Page<Post> getPostsByAuthorID(long authorID, Pageable pageable) {
        return postRepository.findByAuthorID(authorID, pageable);
    }

    public Page<Post> getAllPostsByPage(Pageable pageable){
        return postRepository.findAll(pageable);
    }

}
