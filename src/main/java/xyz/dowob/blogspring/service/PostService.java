package xyz.dowob.blogspring.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import xyz.dowob.blogspring.Exceptions.Postdata_UpdateException;
import xyz.dowob.blogspring.functions.ArticleDto;
import xyz.dowob.blogspring.functions.DeltaToJsonConverter;
import xyz.dowob.blogspring.functions.EditorMethod;
import xyz.dowob.blogspring.functions.PublishRequestDto;
import xyz.dowob.blogspring.model.Post;
import xyz.dowob.blogspring.model.User;
import xyz.dowob.blogspring.repository.PostRepository;
import xyz.dowob.blogspring.repository.UserRepository;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final DeltaToJsonConverter deltaToJsonConverter = new DeltaToJsonConverter();

    private final int MAX_CONTENT_LENGTH = 5500000;

    @Autowired
    public PostService(PostRepository postRepository, UserRepository userRepository){
        this.postRepository = postRepository;
        this.userRepository = userRepository;

    }
    @Transactional(rollbackFor = {Postdata_UpdateException.class, JsonProcessingException.class})
    public Long addNewPost(String username) throws Postdata_UpdateException, JsonProcessingException {
        if (username == null || username.trim().isEmpty()) {
            throw new Postdata_UpdateException(Postdata_UpdateException.ErrorCode.DID_NOT_LOGIN);
        }
        Post post = new Post();


        User author = userRepository.findByUsername(username).orElseThrow(() -> new Postdata_UpdateException(Postdata_UpdateException.ErrorCode.NOT_FOUND_USER));
        post.setAuthor(author);

        post.setTitle("預設標題");
        post.setContent("{\"delta\":[{\"insert\":\"預設內容\\n\"}]}");
        postRepository.save(post);

        return post.getArticleId();
    }
    public void updatePostWithContent(Long articleId, ArticleDto articleDto) throws Postdata_UpdateException, JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        Post post = getPostByArticleId(articleId);

        String stringTitle = articleDto.getTitle();
        if (stringTitle == null || stringTitle.isBlank() ) {
            throw new Postdata_UpdateException(Postdata_UpdateException.ErrorCode.POST_INVALID);
        }
        if (stringTitle.length() > 250) {
            throw new Postdata_UpdateException(Postdata_UpdateException.ErrorCode.TITLE_TOO_LONG);
        }
        post.setTitle(stringTitle);

        List<Map<String, Object>> standardContents = deltaToJsonConverter.convertToStandardDeltaFormat(articleDto.getDeltaContent());
        String convertContent = deltaToJsonConverter.convertArticleDeltaToJson(standardContents);

        Map<String, Object> map = objectMapper.readValue(convertContent, new TypeReference<>() {
        });
        Object opsValue = map.get("delta");
        Map<String, Object> modifiedContent = new HashMap<>();
        modifiedContent.put("ops", opsValue);


        if  (convertContent == null || convertContent.isBlank() || EditorMethod.isOnlyWhiteSpaceOrEmpty(modifiedContent)) {
            throw new Postdata_UpdateException(Postdata_UpdateException.ErrorCode.POST_INVALID);
        }else if (convertContent.length() > MAX_CONTENT_LENGTH) {
            throw new Postdata_UpdateException(Postdata_UpdateException.ErrorCode.CONTENT_TOO_LONG);
        }


        post.setContent(convertContent);
        postRepository.save(post);
    }

    public void deletePost(Long articleId, String username) throws Postdata_UpdateException {
        Post post = getPostByArticleId(articleId);
        if (post.getAuthor().getUsername().equals(username)) {
            post.setDeleted(true);
            postRepository.save(post);
        } else {
            throw new Postdata_UpdateException(Postdata_UpdateException.ErrorCode.NOT_AUTHORIZED);
        }

    }



    public Map<String, String> saveNewArticleImage(MultipartFile file, Long articleId) throws IOException {
        return EditorMethod.saveImage(file, articleId);
    }

    public void deleteArticleImage(String imageUrl) throws Postdata_UpdateException {
        EditorMethod.deleteImage(imageUrl);
    }


    public Map<String, Object> convertPostStructure(String commentContent, Post post) throws JsonProcessingException {
        return deltaToJsonConverter.convertArticleFromJsonToDelta(commentContent, post);
    }

    public void updatePostPublish(PublishRequestDto publishRequest) throws Postdata_UpdateException {
        try {
            Post post = getPostByArticleId(publishRequest.getId());
            post.setPublished(publishRequest.getIsPublished());
            postRepository.save(post);
        } catch (Postdata_UpdateException e) {
            throw new Postdata_UpdateException(Postdata_UpdateException.ErrorCode.POST_UPDATE_FAILED);
        }
    }



    public List<Post> getLatestSixPosts(){
        Pageable pageable = PageRequest.of(0, 6, Sort.by("articleId").descending());
        return postRepository.findByPublishedTrueAndDeletedFalse(pageable).getContent();
    }

    public Page<Post> getPublishedPostsByPage(Pageable pageable){
        return postRepository.findByPublishedTrueAndDeletedFalse(pageable);
    }


    @Transactional
    public Post getPostByArticleId(Long articleId) throws Postdata_UpdateException {
        Post post = postRepository.findByArticleId(articleId)
                .orElseThrow(() -> new Postdata_UpdateException(Postdata_UpdateException.ErrorCode.POST_NOT_FOUND));
        if (post != null) {
            Hibernate.initialize(post.getAuthor());
        }
        return post;
    }

    public User getUserByArticleId(Long articleId) {
        return userRepository.findByPostsArticleId(articleId)
                .orElseThrow(() -> new UsernameNotFoundException("找不到ID為" + articleId + "的使用者。"));
    }

    public Page<Post> getPostsByAuthorId(long authorId, Pageable pageable, boolean isPublished) {
        if (isPublished) {
            return postRepository.findByPublishedTrueAndAuthorIdAndDeletedFalse(authorId, pageable);
        }else {
            return postRepository.findByAuthorID(authorId, pageable);
        }
    }
}
