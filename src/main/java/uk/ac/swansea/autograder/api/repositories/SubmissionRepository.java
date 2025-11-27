package uk.ac.swansea.autograder.api.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import uk.ac.swansea.autograder.api.entities.Submission;

public interface SubmissionRepository extends PagingAndSortingRepository<Submission, Long>, CrudRepository<Submission, Long> {
    Page<Submission> findAllByProblemIdAndUserId(Long problemId, Long userId, Pageable pageable);

    Page<Submission> findAllByUserId(Long userId, Pageable pageable);

    Page<Submission> findAllByProblemId(Long problemId, Pageable pageable);
}