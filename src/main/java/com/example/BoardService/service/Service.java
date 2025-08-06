package com.example.BoardService.service;

import com.example.BoardService.dto.*;
import com.example.BoardService.entity.Comment;
import com.example.BoardService.entity.Media;
import com.example.BoardService.entity.Post;
import com.example.BoardService.entity.User;
import com.example.BoardService.repository.CommentRepository;
import com.example.BoardService.repository.MediaRepository;
import com.example.BoardService.repository.PostRepository;
import com.example.BoardService.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@Transactional
@Slf4j
public class Service {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private MediaRepository mediaRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public List<PostDTO> showPostsService() {
        //user_id를 제외한 모든 엔티티를 DTO로 변환해야 한다.
        List<Post> postList = postRepository.findAll();
        List<PostDTO> postDTOList = new ArrayList<>();

        for (Post post : postList) {
            postDTOList.add(post.toDTO());
        }
        log.info("/posts GET Request:Service logic sucess");
        return postDTOList;
    }

    //media 결합 완료
    //25/08/05-post에 유저 연결하는 로직 추가
    public PostAndMediaDTO createPost(PostDTO postDTO, List<MediaDTO> mediaDTOList) {
        Post postEntity = postDTO.toEntity();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        postEntity.setUser(userRepository.findByUsername(auth.getName()));
        postEntity.setPostTime(LocalDateTime.now());

        //postrepository에 postEntity저장
        Post savedPostEntity = postRepository.save(postDTO.toEntity());

        //mediaRepository에 mediaEntityList저장
        List<Media> savedMediaList = mediaRepository.saveAll(
                mediaDTOList.stream()
                        .map(MediaDTO::toEntity)
                        .collect(Collectors.toList())
        );

        //media와 post연결(setter)
        for (Media media : savedMediaList)
            media.setPost(savedPostEntity);

        //저장후 postandmediadto로 변환하여 리턴
        log.info("/post POST Request:Service logic sucess");
        return new PostAndMediaDTO(savedPostEntity.toDTO(), savedMediaList.stream()
                .map(Media::toDTO)
                .collect(Collectors.toList())
        );
    }

    //media 결합 완료
    //user 정보 확인하여 작성했던 user와 다르다면 수정하지 않는 로직 추가 필요
    //25/08/06 user 일치 여부 로직 추가 완료
    public PostAndMediaDTO updatePost(Long postId, PostAndMediaDTO inputPostAndMediaDTO) throws AccessDeniedException{
        /*작성유저 정보와 수정 요청 유저 정보가 다르면 수정하지 않음
        받아온 inputDto에는 postID가 없다.
        받아온 DTO를 post와 media로 분리V
        분리한 dto를 엔티티로 변환V
        post엔티티는 리파지토리에서 꺼내온 데이터와 들어온 데이터를 merge해서 리파지토리에 저장한다.V
        media엔티티는 리파지토리에서 꺼내온 데이터와 비교해야 한다.-메소드로 추상화
        수정을 완료한 entity를 dto로 변환하면서 postandmediadto에 담아서 리턴*/
        Post existingPostEntity = postRepository.findByIdOrElseThrow(postId);

        //0. 작성유저 정보와 수정 요청 유저 정보가 다르면 수정하지 않음
        String requestUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if(!existingPostEntity.getUser().getUsername().equals(requestUsername))
            throw new AccessDeniedException("글을 작성한 사용자만 수정이 가능합니다.");
        else {
            //1. 받아온 dto post와 media로 분리+엔티티로 변환
            Post inputPostEntity = inputPostAndMediaDTO.getPostDTO().toEntity();
            inputPostEntity.setPostId(postId);
            List<Media> inputMediaEntityList = inputPostAndMediaDTO.getMediaDTOList().stream()
                    .map(MediaDTO::toEntity)
                    .peek(media -> media.setPost(inputPostEntity))
                    .collect(Collectors.toList());

            //2. post엔티티 merge해서 저장
            Post mergedPostEntity = mergePostEntity(existingPostEntity, inputPostEntity);
            Post savedPostEntity = postRepository.save(mergedPostEntity);

            //3. media엔티티 로직 거쳐서 병합,저장,삭제
            List<Media> mergedMediaEntityList = mergeMediaEntityList(mediaRepository.findAllByPostPostId(postId), inputMediaEntityList);
            List<Media> savedMediaEntityList = mediaRepository.saveAll(mergedMediaEntityList);

            //4. postandmediadto에 담아서 수정 완료된 값 리턴
            return new PostAndMediaDTO(savedPostEntity.toDTO(),
                    savedMediaEntityList.stream()
                            .map(Media::toDTO)
                            .collect(Collectors.toList()));

        }
    }

    //media 결합 완료
    //comment 결합 완료
    public void deletePost(Long postId) {
        postRepository.deleteById(postId);
        mediaRepository.deleteAllByPostPostId(postId);
        commentRepository.deleteAllByPostPostId(postId);
    }

    //media 결합 완료
    //comment 결합 완료
    public PostAndMediaAndCommentDTO showPost(Long postId) {

        Post post = postRepository.findByIdOrElseThrow(postId);
        List<Media> mediaList = mediaRepository.findAllByPostPostId(postId);
        List<Comment> commentList = commentRepository.findAllByPostPostId(postId);

        //DTO 합침
        List<MediaDTO> mediaDTOList = mediaList.stream()
                .map(Media::toDTO)
                .collect(Collectors.toList());
        List<CommentDTO> commentDTOList = commentList.stream()
                .map(Comment::toDTO)
                .collect(Collectors.toList());

        PostAndMediaAndCommentDTO postAndMediaAndCommentDTO =
                new PostAndMediaAndCommentDTO(post.toDTO(), mediaDTOList, commentDTOList);

        return postAndMediaAndCommentDTO;

    }
    //글
    //----------------------------------------------------------------------------------------------------------------
    //댓글
    //25/08/05-comment에 user 정보 삽입하도록 수정
    public CommentDTO createComment(Long postId, CommentDTO createCommentRequestDTO) {
        //dto를 엔티티로 변환 -> post와 연결 -> repository에 저장 -> DTO로 바꿔서 return
        //엔티티로 변환
        Comment inputCommentEntity = createCommentRequestDTO.toEntity();

        //필요한 정보 삽입
        inputCommentEntity.setCommentTime(LocalDateTime.now());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        inputCommentEntity.setUser(userRepository.findByUsername(auth.getName()));
        inputCommentEntity.setPost(postRepository.findByIdOrElseThrow(postId));

        //저장
        Comment savedCommentEntity = commentRepository.save(inputCommentEntity);

        return savedCommentEntity.toDTO();
    }

    //user 정보 확인하여 작성했던 user와 다르다면 수정하지 않는 로직 추가 필요
    //25/08/06-user 일치 여부 로직 추가 완료
    public CommentDTO updateComment(Long commentId,CommentDTO updateCommentRequestDTO) throws AccessDeniedException{
        //1. DTO를 엔티티로 변환
        Comment updateCommentRequestEntity = updateCommentRequestDTO.toEntity();
        //2. 기존에 존재하는 엔티티 꺼내옴
        Comment existingCommentEntity = commentRepository.findByIdOrElseThrow(commentId);
        //3. 유저 정보 비교해서 다르면 throws
        String requestUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        if(!existingCommentEntity.getUser().getUsername().equals(requestUsername))
            throw new AccessDeniedException("댓글을 작성한 사용자만 수정이 가능합니다.");
        else {
            //4. 2개의 엔티티를 병합 4. 병합된 엔티티를 리파지터리에 저장
            Comment savedCommentEntity =
                    commentRepository.save(mergeComment(updateCommentRequestEntity, existingCommentEntity));
            return savedCommentEntity.toDTO();
        }

    }
    //댓글

    //25/08/04-password encode하여 저장하도록 변경
    public void joinUser(UserDTO saveRequestUserDTO) {
        User saveRequestUserEntity = saveRequestUserDTO.toEntity();
        saveRequestUserEntity.setUserTime(LocalDateTime.now());
        saveRequestUserEntity.setPassword(
                bCryptPasswordEncoder.encode(saveRequestUserEntity.getPassword()));
        userRepository.save(saveRequestUserDTO.toEntity());
    }

    //25/08/04-password 검증 로직 추가
    public Boolean loginUser(UserDTO loginUserRequestDTO) {
        User loginUserRequestEntity = loginUserRequestDTO.toEntity();
        User existingUserEntity = userRepository.findByUsername(loginUserRequestEntity.getUsername());

        if(existingUserEntity == null)
            return false;

        return bCryptPasswordEncoder.matches(loginUserRequestEntity.getPassword(),existingUserEntity.getPassword());
    }
    //유저
    //----------------------------------------------------------------------------------------------------------------
    //내부 메소드
    private Post mergePostEntity(Post targetPostEntity, Post inputPostEntity) {
        boolean hasChanged = false;
        //title,content 새로운 것만 target에 넣기
        if (!inputPostEntity.getPostTitle().isEmpty() || inputPostEntity.getPostContent() != null) {
            targetPostEntity.setPostTitle(inputPostEntity.getPostTitle());
            hasChanged = true;
        }
        if (!inputPostEntity.getPostContent().isEmpty() || inputPostEntity.getPostContent() != null) {
            targetPostEntity.setPostContent(inputPostEntity.getPostContent());
            hasChanged = true;
        }

        if (hasChanged)
            targetPostEntity.setPostTime(LocalDateTime.now());

        return targetPostEntity;
    }

    private List<Media> mergeMediaEntityList(List<Media> existingMediaEntityList, List<Media> inputMediaEntityList) {
        /*input데이터에는 없는데 리파지토리 데이터에 있으면 삭제
          input데이터에도 있고 리파지토리 데이터에도 있으면 merge해서 리파지토리에 저장
          input데이터가 있지만 리파지토리 데이터가 없으면 신규 media로 하고 리파지토리에 저장
          0. 리파지토리에 데이터가 없는데 input으로 media들어오면 전체 저장
          1. 리파지토리에 데이터가 있는데 input으로 아무것도 들어오지 않으면 전체 삭제
          2. Map 사용해서 input과 리파지토리의 id가 같으면 연결
          2.1 연결된 것중 변한 것이 있다면 merge해서 저장
          3. Map으로 연결 되지 않은 meida 데이터가 남음
          3.1 Map으로 연결된 것보다 작은 id값이 input에는 없고,리파지토리에는 있을 때
          -> 리파지토리에서 삭제해야함 -> 따로 분리해서 삭제
          3.2 Map으로 연결된 것보다 큰 userId 값이 inputd에는 있고, 리파지토리에는 없을 때-> 리파지토리에 추가해야함
          */

        //1. 리파지토리에 데이터가 있는데 input으로 아무것도 들어오지 않으면 전체 삭제
        if (inputMediaEntityList == null || inputMediaEntityList.isEmpty()) {
            existingMediaEntityList.clear();
            return existingMediaEntityList;
        }

        //0. 리파지토리에 데이터가 없는데 input으로 media들어오면 전체 저장
        if (existingMediaEntityList == null || existingMediaEntityList.isEmpty())
            return inputMediaEntityList;

        //2. Map으로 전체 연결
        Map<Long, Media> existingMediaMap = existingMediaEntityList.stream()
                .collect(Collectors.toMap(Media::getMediaId, media -> media));

        //3. 수정 완료된 데이터 저장할 리스트 생성
        List<Media> updatedMediaList = new ArrayList<>();

        //4. 업데이트 할 데이터와 삭제할 데이터를 나눠서 저장함

        for (Media inputMedia : inputMediaEntityList) {
            //existing과 input의 id가 같다면 병합
            Long id = inputMedia.getMediaId();
            if (existingMediaMap.containsKey(id)) {
                updatedMediaList.add(mergeMeida(inputMedia, existingMediaMap.get(id)));
                existingMediaMap.remove(id); //삭제해야할 미디어만 existingMap에 남게한다.
            } else {//두개의 id가 다른 것중 inputMeida에만 있는 것,즉 리파지토리에 넣어야할 데이터만 updatedMediaList에 넣는다.
                updatedMediaList.add(inputMedia);
            }
        }

        //5. 삭제할 데이터를 리파지토리에서 삭제함
        mediaRepository.deleteAllById(
                existingMediaMap.values().stream()
                        .map(Media::getMediaId)
                        .collect(Collectors.toList())
        );

        //6. return
        return updatedMediaList;
    }

    private Media mergeMeida(Media inputMedia, Media media) {
        if (!inputMedia.getMediaType().equals(media.getMediaType()))
            media.setMediaType(inputMedia.getMediaType());
        if (!inputMedia.getMediaContent().equals(media.getMediaContent()))
            media.setMediaContent(inputMedia.getMediaContent());

        return media;
    }

    private Comment mergeComment(Comment updateCommentRequestEntity, Comment existingCommentEntity) {
        if(!updateCommentRequestEntity.getCommentContent().isEmpty() || updateCommentRequestEntity.getCommentContent() != null){
            existingCommentEntity.setCommentContent(updateCommentRequestEntity.getCommentContent());
            existingCommentEntity.setCommentTime(LocalDateTime.now());
        }
        return existingCommentEntity;
    }

}

