package test_support.entity.lazyloading;

import io.jmix.data.entity.StandardEntity;

import javax.persistence.*;
import java.util.List;

@Table(name = "TEST_MANY_TO_MANY_SECOND_ENTITY")
@Entity(name = "test_ManyToManySecondEntity")
public class ManyToManySecondEntity extends StandardEntity {
    @Column(name = "NAME")
    protected String name;

    @JoinTable(name = "TEST_MANY_TO_MANY_FIRST_ENTITY_MANY_TO_MANY_SECOND_ENTITY_LINK",
            joinColumns = @JoinColumn(name = "MANY_TO_MANY_SECOND_ENTITY_ID"),
            inverseJoinColumns = @JoinColumn(name = "MANY_TO_MANY_FIRST_ENTITY_ID"))
    @ManyToMany(fetch = FetchType.LAZY)
    protected List<ManyToManyFirstEntity> manyToManyFirstEntities;

    public List<ManyToManyFirstEntity> getManyToManyFirstEntities() {
        return manyToManyFirstEntities;
    }

    public void setManyToManyFirstEntities(List<ManyToManyFirstEntity> manyToManyFirstEntities) {
        this.manyToManyFirstEntities = manyToManyFirstEntities;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}