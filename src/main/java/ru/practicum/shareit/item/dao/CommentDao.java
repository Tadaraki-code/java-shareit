package ru.practicum.shareit.item.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Comment;

import java.util.Collection;

public interface CommentDao extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c WHERE c.item.id = :itemId")
    Collection<Comment> findAllCommentsByItemId(@Param("itemId") Long itemId);

    @Query("SELECT c FROM Comment c WHERE c.item.id IN :itemIds")
    Collection<Comment> findAllCommentsForAllItemsById(@Param("itemIds") Collection<Long> itemIds);
}
