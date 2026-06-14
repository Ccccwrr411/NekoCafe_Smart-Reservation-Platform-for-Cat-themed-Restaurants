#!/usr/bin/env python3
# insert_review_rating.py
# 往 ReviewsMapper.xml 末尾 </mapper> 前插入评分查询

filepath = '/d/code2023/work/2026work/ruanjiangongcheng/yunying/NekoCafe_Smart-Reservation-Platform-for-Cat-themed-Restaurants/backend/src/main/resources/mapper/ReviewsMapper.xml'

with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

insert_block = """
  <!-- ══ 总部运营：按门店查询平均评分 ══ -->
  <select id="avgRatingByStoreId" parameterType="java.lang.Integer" resultType="java.lang.Double">
    SELECT AVG(overall_rating)
    FROM public.reviews
    WHERE store_id = #{storeId,jdbcType=INTEGER}
      AND status = 'VISIBLE'
  </select>

"""

new_content = content.replace('</mapper>', insert_block + '</mapper>')

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(new_content)

print('Done: inserted avgRatingByStoreId query into ReviewsMapper.xml')
