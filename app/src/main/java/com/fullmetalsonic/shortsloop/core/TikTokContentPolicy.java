package com.fullmetalsonic.shortsloop.core;

import java.util.ArrayList;
import java.util.List;
import com.fullmetalsonic.shortsloop.core.TikTokStructurePolicy.Match;
import com.fullmetalsonic.shortsloop.core.TikTokStructurePolicy.Node;
import static com.fullmetalsonic.shortsloop.core.TikTokStructurePolicy.*;

/** Content classification after unique recommendation page selection. Unknown media never gets a timer. */
final class TikTokContentPolicy {
    private TikTokContentPolicy() { }

    static Match inspect(List<Node> nodes, int pager, int page) {
        Node frame = nodes.get(page);
        int ad = -1, photoLabel = -1, seek = -1, player = -1, adPlayer = -1;
        List<Integer> renders = new ArrayList<>(), images = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            Node n = nodes.get(i);
            if (!n.visible) continue;
            boolean inPage = descendant(nodes, i, page);
            if (n.type.equals("android.widget.SeekBar")) {
                if (seek >= 0 || !id(n, "vb6") || !ancestor(nodes, i, "video_seek_bar")
                        || !frame.contains(n)) return rejected("tiktok.unsupported");
                seek = i;
            }
            if (!inPage) continue;
            if (specialId(n.id)) return rejected("tiktok.unsupported");
            if (n.adLabel) {
                if (ad >= 0 || !id(n, "i2n") || !n.type.equals("android.widget.Button")
                        || !ancestor(nodes, i, "widget_container") || !frame.contains(n)) return rejected("tiktok.unsupported");
                ad = i;
            }
            if (n.photoLabel) {
                if (photoLabel >= 0 || !id(n, "tv_label") || !n.type.equals("android.widget.TextView")
                        || !ancestor(nodes, i, "widget_container") || !frame.contains(n)) return rejected("tiktok.unsupported");
                photoLabel = i;
            }
            if (id(n, "player_view")) {
                if (player >= 0 || !n.type.equals("android.widget.FrameLayout") || !frame.contains(n)
                        || !ancestor(nodes, i, "video_visible_area_container")
                        || !ancestor(nodes, i, "video_container_area")) return rejected("tiktok.unsupported");
                player = i;
            }
            if (id(n, "player_view_pager")) {
                if (adPlayer >= 0 || n.parent != page || !frame.contains(n)) return rejected("tiktok.unsupported");
                adPlayer = i;
            }
            if (n.type.equals("android.view.TextureView") || n.type.equals("android.view.SurfaceView")) renders.add(i);
            if (id(n, "wel")) images.add(i);
        }
        if (photoLabel >= 0) {
            if (player < 0 || adPlayer >= 0 || !renders.isEmpty() || images.size() != 1)
                return rejected("tiktok.unsupported");
            return photograph(nodes, pager, page, player, images.get(0), seek, ad, photoLabel);
        }
        // A photo subtree without its dedicated label is unknown, not an ordinary clockless video.
        if (!images.isEmpty() || renders.size() != 1 || (player >= 0 && adPlayer >= 0))
            return rejected("tiktok.unsupported");
        int media = renders.get(0);
        Node render = nodes.get(media);
        if (!frame.contains(render) || render.right - render.left < (frame.right - frame.left) * .5)
            return rejected("tiktok.unsupported");
        boolean ordinary = player >= 0 && descendant(nodes, media, player)
                && nodes.get(player).contains(render);
        if (!ordinary) {
            // The ad-only pager also renders dot carousels. Never grant it ordinary playback/timing.
            if (ad < 0 || adPlayer < 0 || player >= 0 || !adRender(nodes, media, adPlayer))
                return rejected("tiktok.unsupported");
        }
        return new Match(pager, page, media, seek, ordinary ? player : adPlayer, -1,
                -1, ad, -1, ordinary, false, null, "");
    }

    private static boolean adRender(List<Node> nodes, int media, int player) {
        int parent = nodes.get(media).parent;
        return parent >= 0 && nodes.get(parent).parent == player && nodes.get(parent).visible
                && nodes.get(parent).type.equals("android.widget.FrameLayout")
                && nodes.get(player).contains(nodes.get(parent)) && nodes.get(parent).contains(nodes.get(media));
    }

    private static Match photograph(List<Node> nodes, int pager, int page, int player,
            int media, int seek, int ad, int label) {
        Node image = nodes.get(media), playerNode = nodes.get(player);
        int imageFrame = parent(nodes, media, "qzz");
        int slide = imageFrame < 0 ? -1 : parent(nodes, imageFrame, "r04");
        int carousel = slide < 0 ? -1 : parent(nodes, slide, "r0h");
        int content = carousel < 0 ? -1 : parent(nodes, carousel, "qzz");
        if (imageFrame < 0 || slide < 0 || carousel < 0 || content < 0
                || !image.type.equals("android.widget.ImageView")
                || !nodes.get(imageFrame).type.equals("android.widget.FrameLayout")
                || !nodes.get(slide).type.equals("android.view.ViewGroup")
                || !nodes.get(slide).same(playerNode) || !nodes.get(carousel).same(playerNode)
                || !nodes.get(slide).contains(image) || !nodes.get(imageFrame).contains(image)
                || image.right - image.left < (playerNode.right - playerNode.left) * .9
                || image.bottom - image.top < (playerNode.bottom - playerNode.top) * .2)
            return rejected("tiktok.unsupported");
        int contentParent = nodes.get(content).parent;
        if (contentParent != player && (contentParent < 0 || !id(nodes.get(contentParent), "abx")
                || !nodes.get(contentParent).visible || nodes.get(contentParent).parent != player))
            return rejected("tiktok.unsupported");
        int index = -1;
        for (int i = 0; i < nodes.size(); i++) {
            Node n = nodes.get(i);
            if (!n.visible || !descendant(nodes, i, page)) continue;
            if (id(n, "r0h") && i != carousel) return rejected("tiktok.unsupported");
            if (id(n, "r04") && i != slide) return rejected("tiktok.unsupported");
            if (id(n, "r06")) {
                if (index >= 0 || n.parent != content || !n.type.equals("android.widget.LinearLayout")
                        || !playerNode.contains(n)) return rejected("tiktok.unsupported");
                index = i;
            }
        }
        PhotoReelPolicy.Position position = new PhotoReelPolicy.Position(0, 0);
        if (index >= 0) {
            List<Node> children = new ArrayList<>();
            for (Node n : nodes) if (n.parent == index) children.add(n);
            if (children.size() != 3) return rejected("tiktok.unsupported");
            for (Node n : children) if (!n.visible || !n.type.equals("android.widget.TextView")
                    || !nodes.get(index).contains(n)) return rejected("tiktok.unsupported");
            position = TikTokPhotoIndexParser.position(children.get(0).number, children.get(1).slash, children.get(2).number);
            if (position == null) return rejected("tiktok.unsupported");
        }
        return new Match(pager, page, media, seek, player, carousel, index, ad, label,
                false, true, position, "");
    }
    private static int parent(List<Node> nodes, int index, String suffix) {
        int parent = nodes.get(index).parent;
        return parent >= 0 && nodes.get(parent).visible && id(nodes.get(parent), suffix) ? parent : -1;
    }
}
