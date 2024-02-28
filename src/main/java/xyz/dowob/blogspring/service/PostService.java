package xyz.dowob.blogspring.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import xyz.dowob.blogspring.Exceptions.Postdata_UpdateException;
import xyz.dowob.blogspring.functions.ArticleDto;
import xyz.dowob.blogspring.functions.DeltaToJsonConverter;
import xyz.dowob.blogspring.functions.EditorMethod;
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

    @Autowired
    public PostService(PostRepository postRepository, UserRepository userRepository){
        this.postRepository = postRepository;
        this.userRepository = userRepository;

    }
    @Transactional(rollbackFor = {Postdata_UpdateException.class, JsonProcessingException.class})
    public Long addNewPost(String username) throws Postdata_UpdateException, JsonProcessingException {
        if (username == null || username.trim().isEmpty()) throw new Postdata_UpdateException(Postdata_UpdateException.ErrorCode.DID_NOT_LOGIN);
        Post post = new Post();


        User author = userRepository.findByUsername(username).orElseThrow(() -> new Postdata_UpdateException(Postdata_UpdateException.ErrorCode.NOT_FOUND_USER));
        post.setAuthor(author);

        post.setTitle("");
        post.setContent("");
        postRepository.save(post);

        return post.getArticleId();
    }
    public void updatePostWithContent(Long articleId, ArticleDto articleDto) throws Postdata_UpdateException, JsonProcessingException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Post post = getPostByArticle_id(articleId);

            String stringTitle = articleDto.getTitle();
            if (stringTitle == null || stringTitle.isBlank() ) throw new Postdata_UpdateException(Postdata_UpdateException.ErrorCode.POST_INVALID);
            if (stringTitle.length() > 250) throw new Postdata_UpdateException(Postdata_UpdateException.ErrorCode.TITLE_TOO_LONG);
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
            }else if (convertContent.length() > 21000) {
                throw new Postdata_UpdateException(Postdata_UpdateException.ErrorCode.CONTENT_TOO_LONG);
            }


            post.setContent(convertContent); // 更新内容
            postRepository.save(post); // 保存更新
        } catch (Postdata_UpdateException | JsonProcessingException e) {
            postRepository.deleteById(articleId);
            throw e;
        }
    }
    public Map<String, String> saveNewArticleImage(MultipartFile file, Long articleId) throws IOException {
        return EditorMethod.saveImage(file, articleId);
    }


    public Map<String, Object> convertPostStructure(String commentContent, Post post) throws JsonProcessingException {
        return deltaToJsonConverter.convertArticleFromJsonToDelta(commentContent, post);
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
