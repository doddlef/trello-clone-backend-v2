package org.kevin.trello_v2.tasks.mapper

import org.apache.ibatis.annotations.Mapper
import org.kevin.trello_v2.tasks.mapper.queries.TaskListInsertQuery
import org.kevin.trello_v2.tasks.mapper.queries.TaskListSearchQuery
import org.kevin.trello_v2.tasks.mapper.queries.TaskListUpdateQuery
import org.kevin.trello_v2.tasks.model.TaskList

@Mapper
interface TaskListMapper {
    fun insert(query: TaskListInsertQuery): Int
    fun findById(id: Long): TaskList?
    fun search(query: TaskListSearchQuery): List<TaskList>
    fun update(query: TaskListUpdateQuery): Int
}